package com.applet.mpinterface.framework;

import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ConsumerProjectGeneratorTest {

    @TempDir
    Path sandbox;

    @Test
    void generatesConsumerInsideConfiguredSandbox() throws Exception {
        ConsumerProjectGenerator generator = new ConsumerProjectGenerator(sandbox);

        GenerationResult result = generator.generate(
                "weather-consumer",
                "WeatherConsumer",
                9092,
                "public String health() { return \"ok\"; }"
        );

        Path project = sandbox.resolve("weather-consumer");
        assertEquals(project.toAbsolutePath().normalize(), result.getProjectDirectory());
        assertEquals(4, result.getFiles().size());
        assertTrue(Files.isRegularFile(project.resolve("pom.xml")));
        assertTrue(Files.isRegularFile(project.resolve("src/main/resources/application.yaml")));
        Path controller = project.resolve(
                "src/main/java/com/weatherconsumer/mpinterface/controller/Controller.java"
        );
        assertTrue(Files.isRegularFile(controller));
        assertTrue(new String(Files.readAllBytes(controller), StandardCharsets.UTF_8)
                .contains("public String health()"));
    }

    @Test
    void rejectsTraversalAndInvalidIdentifiers() throws Exception {
        ConsumerProjectGenerator generator = new ConsumerProjectGenerator(sandbox);

        assertThrows(IllegalArgumentException.class, () -> generator.generate(
                "../outside", "WeatherConsumer", 9092, ""
        ));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(
                "consumer", "weather-consumer", 9092, ""
        ));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(
                "consumer", "WeatherConsumer", 70000, ""
        ));
    }

    @Test
    void rejectsProjectDirectorySymbolicLink() throws Exception {
        Path outside = Files.createDirectory(sandbox.resolve("real-project"));
        Files.createSymbolicLink(sandbox.resolve("consumer"), outside);
        ConsumerProjectGenerator generator = new ConsumerProjectGenerator(sandbox);

        assertThrows(IllegalArgumentException.class, () -> generator.generate(
                "consumer", "WeatherConsumer", 9092, ""
        ));
    }

    @Test
    void neverOverwritesAnExistingProject() throws Exception {
        Files.createDirectory(sandbox.resolve("consumer"));
        ConsumerProjectGenerator generator = new ConsumerProjectGenerator(sandbox);

        assertThrows(IllegalArgumentException.class, () -> generator.generate(
                "consumer", "WeatherConsumer", 9092, ""
        ));
    }

    @Test
    void concurrentGenerationOfSameProjectHasExactlyOneWinner() throws Exception {
        ConsumerProjectGenerator generator = new ConsumerProjectGenerator(sandbox);
        int contenderCount = 8;
        ExecutorService workers = Executors.newFixedThreadPool(contenderCount);
        CountDownLatch ready = new CountDownLatch(contenderCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Boolean>> attempts = new ArrayList<>();
        for (int attempt = 0; attempt < contenderCount; attempt++) {
            final int port = 9100 + attempt;
            attempts.add(() -> {
                ready.countDown();
                start.await();
                try {
                    generator.generate("shared-consumer", "SharedConsumer", port, "");
                    return true;
                } catch (IllegalArgumentException expected) {
                    return false;
                }
            });
        }

        List<Future<Boolean>> results = new ArrayList<>();
        for (Callable<Boolean> attempt : attempts) results.add(workers.submit(attempt));
        ready.await();
        start.countDown();
        int winners = 0;
        for (Future<Boolean> result : results) if (result.get()) winners++;
        workers.shutdownNow();

        assertEquals(1, winners);
        assertTrue(Files.isRegularFile(sandbox.resolve("shared-consumer/pom.xml")));
    }

    @Test
    void removesReservedDirectoryWhenRenderingFails() throws Exception {
        Configuration missingTemplates = new Configuration(Configuration.VERSION_2_3_31);
        ConsumerProjectGenerator generator = new ConsumerProjectGenerator(sandbox, missingTemplates);

        assertThrows(java.io.IOException.class,
                () -> generator.generate("failed-consumer", "FailedConsumer", 9102, ""));

        assertTrue(Files.notExists(sandbox.resolve("failed-consumer")));
        ConsumerProjectGenerator retry = new ConsumerProjectGenerator(sandbox);
        GenerationResult recovered = retry.generate("failed-consumer", "FailedConsumer", 9102, "");
        assertEquals(4, recovered.getFiles().size());
    }

    @Test
    void httpAdapterGeneratesManifestAndRejectsUnsafePayload() throws Exception {
        MockMvc http = MockMvcBuilders.standaloneSetup(new ConsumerGenerator(sandbox.toString())).build();

        String response = http.perform(post("/genConsumer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectName\":\"http-consumer\",\"appletName\":\"HttpConsumer\","
                                + "\"portNum\":9093,\"javaCode\":\"public String health() { return \\\"ok\\\"; }\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files.length()").value(4))
                .andReturn().getResponse().getContentAsString();

        assertTrue(response.contains("\"projectName\":\"http-consumer\""));
        assertFalse(response.contains(sandbox.toAbsolutePath().normalize().toString()));
        assertFalse(response.contains("projectDirectory"));

        http.perform(post("/genConsumer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectName\":\"../outside\",\"appletName\":\"HttpConsumer\","
                                + "\"portNum\":9093,\"javaCode\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void commandLineAdapterGeneratesInspectableFixture() throws Exception {
        Path code = sandbox.resolve("method.java.txt");
        Files.write(code, "public String health() { return \"ok\"; }".getBytes(StandardCharsets.UTF_8));

        GeneratorCli.main(new String[]{
                sandbox.toString(), "cli-consumer", "CliConsumer", "9094", code.toString()
        });

        assertTrue(Files.isRegularFile(sandbox.resolve("cli-consumer/pom.xml")));
        assertTrue(Files.isRegularFile(sandbox.resolve(
                "cli-consumer/src/main/java/com/cliconsumer/mpinterface/controller/Controller.java"
        )));
    }
}
