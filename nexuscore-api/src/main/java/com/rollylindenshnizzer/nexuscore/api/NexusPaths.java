package com.rollylindenshnizzer.nexuscore.api;

import java.nio.file.Path;
import java.util.Objects;

public final class NexusPaths {
    private final Path gameDirectory;
    private final Path configDirectory;
    private final Path generatedResourcesDirectory;

    public NexusPaths(Path gameDirectory, Path configDirectory, Path generatedResourcesDirectory) {
        this.gameDirectory = Objects.requireNonNull(gameDirectory, "gameDirectory");
        this.configDirectory = Objects.requireNonNull(configDirectory, "configDirectory");
        this.generatedResourcesDirectory = Objects.requireNonNull(generatedResourcesDirectory, "generatedResourcesDirectory");
    }

    public static NexusPaths under(Path root) {
        Path absoluteRoot = root.toAbsolutePath().normalize();
        return new NexusPaths(absoluteRoot, absoluteRoot.resolve("config"), absoluteRoot.resolve("generated"));
    }

    public Path gameDirectory() {
        return gameDirectory;
    }

    public Path configDirectory() {
        return configDirectory;
    }

    public Path generatedResourcesDirectory() {
        return generatedResourcesDirectory;
    }
}
