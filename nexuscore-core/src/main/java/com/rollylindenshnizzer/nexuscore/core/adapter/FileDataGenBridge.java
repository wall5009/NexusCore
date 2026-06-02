package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.NexusPaths;
import com.rollylindenshnizzer.nexuscore.api.datagen.NexusDataGen;
import com.rollylindenshnizzer.nexuscore.api.datagen.NexusGeneratedResource;
import com.rollylindenshnizzer.nexuscore.bridge.datagen.DataGenBridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FileDataGenBridge implements DataGenBridge {
    private final NexusPaths paths;

    public FileDataGenBridge(NexusPaths paths) {
        this.paths = paths;
    }

    @Override
    public List<Path> generate(NexusDataGen dataGen) {
        List<Path> written = new ArrayList<>();
        for (NexusGeneratedResource resource : dataGen.resources()) {
            Path path = paths.generatedResourcesDirectory().resolve(resource.path()).normalize();
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, resource.json());
                written.add(path);
            } catch (IOException error) {
                throw new IllegalStateException("NexusCore could not generate resource '" + resource.path() + "'. Reason: " + error.getMessage() + ". Fix: verify the generated resources directory is writable.", error);
            }
        }
        return written;
    }
}
