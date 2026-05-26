package com.rollylindenshnizzer.nexuscore.compat;

import java.util.List;

public record IntegrationDescriptor(String id, List<String> requiredMods, String description) {
    public boolean available() {
        return requiredMods.stream().allMatch(com.rollylindenshnizzer.nexuscore.core.NexusEnvironment::isModLoaded);
    }
}
