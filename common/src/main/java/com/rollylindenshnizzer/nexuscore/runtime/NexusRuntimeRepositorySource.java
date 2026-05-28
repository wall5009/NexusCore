package com.rollylindenshnizzer.nexuscore.runtime;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlags;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@NexusStable(since = "1.3")
public final class NexusRuntimeRepositorySource implements RepositorySource {
    public static final NexusRuntimeRepositorySource INSTANCE = new NexusRuntimeRepositorySource();
    public static final String PACK_ID = "nexuscore_runtime_generated";

    private NexusRuntimeRepositorySource() {
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        PackLocationInfo location = new PackLocationInfo(PACK_ID,
                Component.literal("NexusCore Runtime Content"),
                PackSource.BUILT_IN,
                Optional.empty());
        Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
            @Override
            public NexusGeneratedPackResources openPrimary(PackLocationInfo info) {
                return new NexusGeneratedPackResources(info);
            }

            @Override
            public NexusGeneratedPackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                return new NexusGeneratedPackResources(info);
            }
        };
        Pack.Metadata metadata = new Pack.Metadata(Component.literal("Generated NexusCore runtime content"),
                PackCompatibility.COMPATIBLE,
                FeatureFlags.DEFAULT_FLAGS,
                List.of());
        PackSelectionConfig selection = new PackSelectionConfig(true, Pack.Position.TOP, true);
        consumer.accept(new Pack(location, supplier, metadata, selection));
    }
}
