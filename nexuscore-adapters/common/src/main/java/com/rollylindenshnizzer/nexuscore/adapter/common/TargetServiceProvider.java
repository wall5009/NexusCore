package com.rollylindenshnizzer.nexuscore.adapter.common;

import com.rollylindenshnizzer.nexuscore.api.NexusPaths;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusFeature;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusLoader;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTargetCapabilities;
import com.rollylindenshnizzer.nexuscore.core.adapter.InMemoryServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.adapter.NativeLoaderCommandBridge;
import com.rollylindenshnizzer.nexuscore.core.adapter.NativeLoaderEventBridge;
import com.rollylindenshnizzer.nexuscore.core.adapter.NativeLoaderNetworkBridge;
import com.rollylindenshnizzer.nexuscore.core.adapter.NativeMinecraftFactoryBridge;
import com.rollylindenshnizzer.nexuscore.core.adapter.NativeMinecraftRegistryBridge;
import com.rollylindenshnizzer.nexuscore.core.target.NexusTargets;
import com.rollylindenshnizzer.nexuscore.core.target.SimpleTargetCapabilities;

import java.nio.file.Path;

public class TargetServiceProvider extends InMemoryServiceProvider {
    public TargetServiceProvider(NexusLoader loader, String minecraftVersion) {
        this(target(loader, minecraftVersion), minecraftVersion, paths(loader, minecraftVersion));
    }

    private TargetServiceProvider(NexusTarget target, String minecraftVersion, NexusPaths paths) {
        this(target, minecraftVersion, paths, new NativeMinecraftFactoryBridge(target));
    }

    private TargetServiceProvider(NexusTarget target, String minecraftVersion, NexusPaths paths, NativeMinecraftFactoryBridge factory) {
        super(target, capabilities(minecraftVersion), paths, new NativeMinecraftRegistryBridge(target, factory, factory), new NativeLoaderEventBridge(target), new NativeLoaderNetworkBridge(target), new NativeLoaderCommandBridge(target), factory, factory);
    }

    private static NexusTarget target(NexusLoader loader, String minecraftVersion) {
        NexusTargets.validate(loader, minecraftVersion);
        return new NexusTarget(loader, minecraftVersion, "nexus-normalized", NexusRuntimeSide.COMMON);
    }

    private static NexusTargetCapabilities capabilities(String minecraftVersion) {
        if ("1.20.1".equals(minecraftVersion)) {
            return SimpleTargetCapabilities.of(
                NexusFeature.REGISTRIES,
                NexusFeature.EVENTS,
                NexusFeature.NETWORKING,
                NexusFeature.COMMANDS,
                NexusFeature.CONFIG,
                NexusFeature.DATAGEN,
                NexusFeature.CLIENT_RENDERING,
                NexusFeature.WORLDGEN,
                NexusFeature.CAPABILITIES
            );
        }
        return SimpleTargetCapabilities.allStable();
    }

    private static NexusPaths paths(NexusLoader loader, String minecraftVersion) {
        return NexusPaths.under(Path.of("build", "nexuscore-runtime", NexusTargets.id(loader, minecraftVersion)));
    }
}
