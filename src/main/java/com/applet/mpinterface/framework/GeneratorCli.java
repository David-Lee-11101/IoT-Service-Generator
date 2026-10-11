package com.applet.mpinterface.framework;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class GeneratorCli {
    private GeneratorCli() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            throw new IllegalArgumentException(
                    "Usage: GeneratorCli <output-root> <project-name> <applet-name> <port> <java-code-file>"
            );
        }
        Path codeFile = Paths.get(args[4]).toAbsolutePath().normalize();
        String javaCode = new String(Files.readAllBytes(codeFile), StandardCharsets.UTF_8);
        ConsumerProjectGenerator generator = new ConsumerProjectGenerator(Paths.get(args[0]));
        GenerationResult result = generator.generate(args[1], args[2], Integer.valueOf(args[3]), javaCode);
        for (String file : result.getFiles()) {
            System.out.println(file);
        }
    }
}
