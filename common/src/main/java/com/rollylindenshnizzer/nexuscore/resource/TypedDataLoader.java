package com.rollylindenshnizzer.nexuscore.resource;

import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Function;

@NexusStable(since = "1.2")
public final class TypedDataLoader<T> {
    private final String folder;
    private final JsonSchema schema;
    private final Function<JsonObject, T> decoder;

    public TypedDataLoader(String folder, JsonSchema schema, Function<JsonObject, T> decoder) {
        this.folder = Objects.requireNonNull(folder, "folder");
        this.schema = schema == null ? new JsonSchema() : schema;
        this.decoder = Objects.requireNonNull(decoder, "decoder");
    }

    public String folder() {
        return folder;
    }

    public Loaded<T> load(ResourceLocation id, JsonObject json) {
        var errors = schema.validate(json);
        if (!errors.isEmpty()) {
            return new Loaded<>(id, null, errors);
        }
        return new Loaded<>(id, decoder.apply(json), java.util.List.of());
    }

    public record Loaded<T>(ResourceLocation id, T value, java.util.List<String> errors) {
        public boolean passed() {
            return errors == null || errors.isEmpty();
        }
    }
}
