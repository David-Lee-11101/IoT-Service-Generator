package com.applet.mpinterface.framework;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GenerationResult {
    private final String projectName;
    private final Path projectDirectory;
    private final List<String> files;

    public GenerationResult(String projectName, Path projectDirectory, List<String> files) {
        this.projectName = projectName;
        this.projectDirectory = projectDirectory;
        this.files = Collections.unmodifiableList(new ArrayList<>(files));
    }

    public String getProjectName() { return projectName; }
    @JsonIgnore
    public Path getProjectDirectory() { return projectDirectory; }
    public List<String> getFiles() { return files; }
}
