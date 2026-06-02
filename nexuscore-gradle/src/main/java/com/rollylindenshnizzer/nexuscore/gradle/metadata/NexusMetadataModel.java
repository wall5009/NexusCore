package com.rollylindenshnizzer.nexuscore.gradle.metadata;

import java.util.List;

public record NexusMetadataModel(
    String modId,
    String displayName,
    String description,
    String version,
    String license,
    List<String> authors,
    String loader,
    String minecraftVersion,
    String targetId
) {
}
