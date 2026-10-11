package com.applet.mpinterface.framework;

import com.applet.mpinterface.Request.CodeRequest;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

@RestController
public class ConsumerGenerator {
    private final ConsumerProjectGenerator projectGenerator;

    public ConsumerGenerator(@Value("${generator.output-root:${GENERATOR_OUTPUT_ROOT:./generated}}") String outputRoot)
            throws IOException {
        this.projectGenerator = new ConsumerProjectGenerator(Paths.get(outputRoot));
    }

    @PostMapping("/genConsumer")
    public GenerationResult executeCode(@RequestBody CodeRequest request) throws IOException, TemplateException {
        return projectGenerator.generate(
                request.getProjectName(), request.getAppletName(), request.getPortNum(), request.getJavaCode()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidRequest(IllegalArgumentException error) {
        return Collections.singletonMap("error", error.getMessage());
    }
}
