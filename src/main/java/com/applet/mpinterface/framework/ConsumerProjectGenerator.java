package com.applet.mpinterface.framework;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ConsumerProjectGenerator {
    private static final Pattern PROJECT_NAME = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._-]{0,63}");
    private static final Pattern JAVA_IDENTIFIER = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]{0,63}");
    private final Path outputRoot;
    private final Configuration templates;

    public ConsumerProjectGenerator(Path outputRoot) throws IOException {
        this(outputRoot, createTemplateConfiguration());
    }

    ConsumerProjectGenerator(Path outputRoot, Configuration templates) throws IOException {
        this.outputRoot = outputRoot.toAbsolutePath().normalize();
        Files.createDirectories(this.outputRoot);
        if (Files.isSymbolicLink(this.outputRoot)) {
            throw new IllegalArgumentException("outputRoot must not be a symbolic link");
        }
        this.templates = templates;
    }

    public GenerationResult generate(String projectName, String appletName, Integer port, String javaCode)
            throws IOException, TemplateException {
        requireMatch(projectName, PROJECT_NAME, "projectName");
        requireMatch(appletName, JAVA_IDENTIFIER, "appletName");
        if (port == null || port < 1 || port > 65535) {
            throw new IllegalArgumentException("portNum must be between 1 and 65535");
        }
        Path projectDirectory = outputRoot.resolve(projectName).normalize();
        if (!projectDirectory.startsWith(outputRoot)) {
            throw new IllegalArgumentException("projectName resolves outside the configured output root");
        }
        String packageName = appletName.toLowerCase(Locale.ROOT);
        Map<String, Object> model = new HashMap<>();
        model.put("appletName", appletName);
        model.put("packageName", packageName);
        model.put("portNum", port);
        model.put("javaCode", javaCode == null ? "" : javaCode);
        List<String> generated = new ArrayList<>();
        reserveProjectDirectory(projectDirectory);
        try {
            render(projectDirectory, "con_pom_template.ftl", "pom.xml", model, generated);
            render(projectDirectory, "con_yaml_template.ftl", "src/main/resources/application.yaml", model, generated);
            render(projectDirectory, "con_main_template.ftl", "src/main/java/com/" + packageName
                    + "/mpinterface/MpInterfaceApplication.java", model, generated);
            render(projectDirectory, "consumer_controller_template.ftl", "src/main/java/com/" + packageName
                    + "/mpinterface/controller/Controller.java", model, generated);
            return new GenerationResult(projectName, projectDirectory, generated);
        } catch (IOException | TemplateException | RuntimeException failure) {
            try {
                deleteReservedDirectory(projectDirectory);
            } catch (IOException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private void render(Path projectDirectory, String templateName, String relativeOutput,
                        Map<String, Object> model, List<String> generated)
            throws IOException, TemplateException {
        Path output = projectDirectory.resolve(relativeOutput).normalize();
        if (!output.startsWith(projectDirectory)) {
            throw new IllegalArgumentException("Generated file resolves outside the project directory");
        }
        requireReservedDirectory(projectDirectory);
        Files.createDirectories(output.getParent());
        requireNoSymbolicLinks(projectDirectory, output.getParent());
        Template template = templates.getTemplate(templateName);
        try (Writer writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            template.process(model, writer);
        }
        generated.add(projectDirectory.relativize(output).toString().replace('\\', '/'));
    }

    private static Configuration createTemplateConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassForTemplateLoading(ConsumerProjectGenerator.class, "/templates");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return configuration;
    }

    private static void reserveProjectDirectory(Path projectDirectory) throws IOException {
        try {
            Files.createDirectory(projectDirectory);
        } catch (FileAlreadyExistsException exists) {
            throw new IllegalArgumentException(
                    "projectName already exists; generation never overwrites a project", exists);
        }
        requireReservedDirectory(projectDirectory);
    }

    private static void requireReservedDirectory(Path projectDirectory) {
        if (Files.isSymbolicLink(projectDirectory) || !Files.isDirectory(projectDirectory)) {
            throw new IllegalArgumentException("reserved project directory was replaced or is not a directory");
        }
    }

    private static void requireNoSymbolicLinks(Path projectDirectory, Path directory) {
        Path current = projectDirectory;
        if (Files.isSymbolicLink(current)) {
            throw new IllegalArgumentException("generated paths must not traverse symbolic links");
        }
        for (Path segment : projectDirectory.relativize(directory)) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) {
                throw new IllegalArgumentException("generated paths must not traverse symbolic links");
            }
        }
    }

    private static void deleteReservedDirectory(Path projectDirectory) throws IOException {
        if (!Files.exists(projectDirectory) && !Files.isSymbolicLink(projectDirectory)) return;
        Files.walkFileTree(projectDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException failure) throws IOException {
                if (failure != null) throw failure;
                Files.deleteIfExists(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void requireMatch(String value, Pattern pattern, String field) {
        if (value == null || !pattern.matcher(value).matches() || value.contains("..")) {
            throw new IllegalArgumentException(field + " contains unsupported characters");
        }
    }
}
