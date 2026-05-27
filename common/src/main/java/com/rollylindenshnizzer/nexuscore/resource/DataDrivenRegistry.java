package com.rollylindenshnizzer.nexuscore.resource;

import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@NexusStable(since = "1.2")
public final class DataDrivenRegistry<T> {
    private final TypedDataLoader<T> loader;
    private final Map<ResourceLocation, T> values = new LinkedHashMap<>();

    public DataDrivenRegistry(TypedDataLoader<T> loader) {
        this.loader = loader;
    }

    public ResourceValidationReport reload(Map<ResourceLocation, JsonObject> jsonObjects) {
        values.clear();
        var loaded = new ArrayList<String>();
        var errors = new ArrayList<String>();
        for (Map.Entry<ResourceLocation, JsonObject> entry : jsonObjects.entrySet()) {
            TypedDataLoader.Loaded<T> result = loader.load(entry.getKey(), entry.getValue());
            if (result.passed()) {
                values.put(entry.getKey(), result.value());
                loaded.add(entry.getKey().toString());
            } else {
                errors.add(entry.getKey() + ": " + result.errors());
            }
        }
        return new ResourceValidationReport(loaded, errors, java.util.List.of());
    }

    public Optional<T> get(ResourceLocation id) {
        return Optional.ofNullable(values.get(id));
    }

    public Map<ResourceLocation, T> values() {
        return Map.copyOf(values);
    }
}
