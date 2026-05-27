package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@NexusStable(since = "1.2")
public final class NexusMachines {
    private static final Map<ResourceLocation, NexusMachineDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        DebugRegistry.section("nexuscore.machines", () -> Integer.toString(DEFINITIONS.size()));
    }

    public static NexusMachineDefinition.Builder machine(String modId, String path) {
        return NexusMachineDefinition.builder(NexusIds.id(modId, path));
    }

    public static NexusMachineDefinition register(NexusMachineDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static Collection<NexusMachineDefinition> definitions() {
        return ListCopy.copy(DEFINITIONS.values());
    }

    public static String debugSummary() {
        return "machines=" + DEFINITIONS.size() + " ids=" + DEFINITIONS.keySet();
    }

    private NexusMachines() {
    }

    private static final class ListCopy {
        private static <T> Collection<T> copy(Collection<T> values) {
            return java.util.List.copyOf(values);
        }
    }
}
