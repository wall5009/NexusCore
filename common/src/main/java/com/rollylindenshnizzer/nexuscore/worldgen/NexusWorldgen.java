package com.rollylindenshnizzer.nexuscore.worldgen;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NexusStable(since = "1.2")
public final class NexusWorldgen {
    private static final Map<String, OreGenerationBuilder> ORES = new LinkedHashMap<>();
    private static final List<Consumer<OreGenerationBuilder>> ORE_LISTENERS = new java.util.ArrayList<>();

    static {
        DebugRegistry.section("nexuscore.worldgen.ores", () -> Integer.toString(ORES.size()));
    }

    public static OreGenerationBuilder ore(String modId, String path) {
        String key = NexusIds.id(modId, path).toString();
        OreGenerationBuilder builder = new OreGenerationBuilder(modId, path);
        ORES.put(key, builder);
        ORE_LISTENERS.forEach(listener -> listener.accept(builder));
        return builder;
    }

    public static void onOreRegistered(Consumer<OreGenerationBuilder> listener) {
        ORE_LISTENERS.add(listener);
        ORES.values().forEach(listener);
    }

    public static List<OreGenerationBuilder> ores() {
        return List.copyOf(ORES.values());
    }

    public static NexusData.DataPlan writeAllTo(NexusData.DataPlan plan) {
        for (OreGenerationBuilder ore : ORES.values()) {
            if (ore.modId().equals(plan.modId())) {
                ore.writeTo(plan);
            }
        }
        return plan;
    }

    public static List<String> oreIds() {
        return List.copyOf(ORES.keySet());
    }

    public static String debugSummary() {
        return "ores=" + ORES.keySet();
    }

    private NexusWorldgen() {
    }
}
