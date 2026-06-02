package com.rollylindenshnizzer.nexuscore.gradle.dsl;

import com.rollylindenshnizzer.nexuscore.gradle.target.NexusTarget;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NexusMinecraftTargetSpec {
    private final String minecraftVersion;
    private final List<String> loaders = new ArrayList<>();

    @Inject
    public NexusMinecraftTargetSpec(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public void loaders(String... loaders) {
        Arrays.stream(loaders)
            .map(loader -> loader.toLowerCase(Locale.ROOT))
            .forEach(this.loaders::add);
    }

    public String minecraftVersion() {
        return minecraftVersion;
    }

    public List<NexusTarget> targets() {
        return loaders.stream().map(loader -> new NexusTarget(loader, minecraftVersion)).toList();
    }
}
