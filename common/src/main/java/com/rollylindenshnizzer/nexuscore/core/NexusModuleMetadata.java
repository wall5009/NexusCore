package com.rollylindenshnizzer.nexuscore.core;

import java.util.List;

public record NexusModuleMetadata(String name,
                                  String version,
                                  List<String> supportedLoaders,
                                  List<String> requiredDependencies,
                                  List<String> optionalDependencies,
                                  String minecraftVersionRange,
                                  String stabilityLevel) {
    public NexusModuleMetadata {
        supportedLoaders = List.copyOf(supportedLoaders);
        requiredDependencies = List.copyOf(requiredDependencies);
        optionalDependencies = List.copyOf(optionalDependencies);
    }
}
