package com.rollylindenshnizzer.nexuscore.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NexusModuleRegistry {
    private static final Map<String, NexusModuleMetadata> MODULES = new LinkedHashMap<>();

    static {
        register(new NexusModuleMetadata("nexus-core", NexusVersion.VERSION, List.of("fabric", "neoforge"),
                List.of("architectury", "owo"), List.of("jei", "emi", "roughlyenoughitems"),
                "[1.21.1,1.22)", "stable"));
        register(new NexusModuleMetadata("nexus-machine", NexusVersion.VERSION, List.of("fabric", "neoforge"),
                List.of("nexus-core"), List.of("jei", "emi", "roughlyenoughitems"),
                "[1.21.1,1.22)", "stable"));
        register(new NexusModuleMetadata("nexus-transfer", NexusVersion.VERSION, List.of("fabric", "neoforge"),
                List.of("nexus-core"), List.of("teamreborn-energy"),
                "[1.21.1,1.22)", "stable"));
        register(new NexusModuleMetadata("nexus-worldgen", NexusVersion.VERSION, List.of("fabric", "neoforge"),
                List.of("nexus-core"), List.of(),
                "[1.21.1,1.22)", "stable"));
        register(new NexusModuleMetadata("nexus-entity", NexusVersion.VERSION, List.of("fabric", "neoforge"),
                List.of("nexus-core"), List.of(),
                "[1.21.1,1.22)", "stable"));
        register(new NexusModuleMetadata("nexus-resource", NexusVersion.VERSION, List.of("fabric", "neoforge"),
                List.of("nexus-core"), List.of(),
                "[1.21.1,1.22)", "stable"));
    }

    public static void register(NexusModuleMetadata metadata) {
        MODULES.put(metadata.name(), metadata);
    }

    public static Collection<NexusModuleMetadata> modules() {
        return List.copyOf(MODULES.values());
    }

    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonArray modules = new JsonArray();
        for (NexusModuleMetadata module : MODULES.values()) {
            JsonObject json = new JsonObject();
            json.addProperty("name", module.name());
            json.addProperty("version", module.version());
            json.add("supportedLoaders", strings(module.supportedLoaders()));
            json.add("requiredDependencies", strings(module.requiredDependencies()));
            json.add("optionalDependencies", strings(module.optionalDependencies()));
            json.addProperty("minecraftVersionRange", module.minecraftVersionRange());
            json.addProperty("stabilityLevel", module.stabilityLevel());
            modules.add(json);
        }
        root.add("modules", modules);
        return root;
    }

    public static List<String> incompatibilityWarnings() {
        return MODULES.values().stream()
                .filter(module -> !NexusVersion.VERSION.equals(module.version()))
                .map(module -> module.name() + " is " + module.version() + " but nexus-core is " + NexusVersion.VERSION)
                .toList();
    }

    private static JsonArray strings(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private NexusModuleRegistry() {
    }
}
