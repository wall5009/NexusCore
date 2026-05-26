package com.rollylindenshnizzer.nexuscore.core;

import dev.architectury.platform.Platform;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record NexusModMetadata(String modId, String version, String name, String description,
                               Collection<String> authors, Collection<String> license,
                               Optional<String> homepage, Optional<String> sources,
                               Optional<String> issueTracker, List<Path> filePaths) {
    public static Optional<NexusModMetadata> read(String modId) {
        return Platform.getOptionalMod(modId).map(mod -> new NexusModMetadata(
                mod.getModId(),
                mod.getVersion(),
                mod.getName(),
                mod.getDescription(),
                mod.getAuthors(),
                mod.getLicense(),
                mod.getHomepage(),
                mod.getSources(),
                mod.getIssueTracker(),
                mod.getFilePaths()
        ));
    }
}
