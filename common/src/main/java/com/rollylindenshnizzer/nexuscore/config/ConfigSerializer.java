package com.rollylindenshnizzer.nexuscore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.core.NexusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class ConfigSerializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static JsonObject toJson(NexusConfig config) {
        JsonObject root = new JsonObject();
        for (ConfigOption<?> option : config.options().values()) {
            root.add(option.key(), GSON.toJsonTree(option.get()));
        }
        return root;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void applyJson(NexusConfig config, JsonObject json) {
        for (Map.Entry<String, ConfigOption<?>> entry : config.options().entrySet()) {
            JsonElement element = json.get(entry.getKey());
            if (element != null) {
                ConfigOption option = entry.getValue();
                Object value = GSON.fromJson(element, option.defaultValue().getClass());
                option.set(value);
            }
        }
        config.validateAll();
    }

    public static void write(NexusConfig config, Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(toJson(config)));
        } catch (IOException exception) {
            throw new NexusException("Failed to write config " + path, exception);
        }
    }

    public static void read(NexusConfig config, Path path) {
        try {
            if (Files.exists(path)) {
                applyJson(config, GSON.fromJson(Files.readString(path), JsonObject.class));
            }
        } catch (IOException exception) {
            throw new NexusException("Failed to read config " + path, exception);
        }
    }

    private ConfigSerializer() {
    }
}
