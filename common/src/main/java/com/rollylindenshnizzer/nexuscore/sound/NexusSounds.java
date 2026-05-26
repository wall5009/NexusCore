package com.rollylindenshnizzer.nexuscore.sound;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class NexusSounds {
    public static RegistrySupplier<SoundEvent> variable(String modId, String path) {
        ResourceLocation id = NexusIds.id(modId, path);
        return NexusRegistries.group(modId).sounds().register(path, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static RegistrySupplier<SoundEvent> fixed(String modId, String path, float range) {
        ResourceLocation id = NexusIds.id(modId, path);
        return NexusRegistries.group(modId).sounds().register(path, () -> SoundEvent.createFixedRangeEvent(id, range));
    }

    public static void subtitle(String modId, String path, String text) {
        NexusData.plan(modId).translation("subtitles." + modId + "." + path.replace('/', '.'), text);
    }

    private NexusSounds() {
    }
}
