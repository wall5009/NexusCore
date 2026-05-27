package com.rollylindenshnizzer.nexuscore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.core.NexusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigSchemaExporter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static JsonObject jsonSchema(NexusConfig config, String schemaVersion) {
        JsonObject root = new JsonObject();
        root.addProperty("modId", config.modId());
        root.addProperty("schemaVersion", schemaVersion);
        JsonArray options = new JsonArray();
        for (ConfigOption<?> option : config.options().values()) {
            JsonObject json = new JsonObject();
            json.addProperty("key", option.key());
            json.addProperty("type", type(option));
            json.add("defaultValue", GSON.toJsonTree(option.defaultValue()));
            json.add("currentValue", GSON.toJsonTree(option.get()));
            json.addProperty("restartRequired", option.restartRequired());
            json.addProperty("worldReloadRequired", option.worldReloadRequired());
            json.addProperty("serverSynced", option.isServerSynced());
            json.addProperty("visible", option.visible());
            json.addProperty("enabled", option.enabled());
            json.addProperty("group", option.group());
            json.addProperty("comment", option.comment());
            json.addProperty("translationKey", option.translationKey().isBlank()
                    ? "config." + config.modId() + "." + option.key()
                    : option.translationKey());
            if (option instanceof IntOption intOption) {
                intOption.min().ifPresent(value -> json.addProperty("min", value));
                intOption.max().ifPresent(value -> json.addProperty("max", value));
            }
            JsonArray dependencies = new JsonArray();
            option.dependencies().forEach(dependencies::add);
            json.add("dependencies", dependencies);
            JsonArray conflicts = new JsonArray();
            option.conflicts().forEach(conflicts::add);
            json.add("conflicts", conflicts);
            options.add(json);
        }
        root.add("options", options);
        return root;
    }

    public static String markdown(NexusConfig config, String schemaVersion) {
        StringBuilder builder = new StringBuilder("# Config Schema: ").append(config.modId()).append("\n\n");
        builder.append("Schema version: `").append(schemaVersion).append("`\n\n");
        builder.append("| Key | Type | Default | Group | Restart | Synced | Description |\n");
        builder.append("| --- | --- | --- | --- | --- | --- | --- |\n");
        for (ConfigOption<?> option : config.options().values()) {
            builder.append("| `").append(option.key()).append("`")
                    .append(" | ").append(type(option))
                    .append(" | `").append(option.defaultValue()).append("`")
                    .append(" | ").append(option.group())
                    .append(" | ").append(option.restartRequired())
                    .append(" | ").append(option.isServerSynced())
                    .append(" | ").append(option.comment())
                    .append(" |\n");
        }
        return builder.toString();
    }

    public static void writeJson(NexusConfig config, String schemaVersion, Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(jsonSchema(config, schemaVersion)));
        } catch (IOException exception) {
            throw new NexusException("Failed to write config schema " + path, exception);
        }
    }

    public static void writeMarkdown(NexusConfig config, String schemaVersion, Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, markdown(config, schemaVersion));
        } catch (IOException exception) {
            throw new NexusException("Failed to write config schema docs " + path, exception);
        }
    }

    private static String type(ConfigOption<?> option) {
        if (option instanceof IntOption) return "integer";
        if (option instanceof BooleanOption) return "boolean";
        if (option instanceof StringOption) return "string";
        if (option instanceof EnumOption<?>) return "enum";
        return option.defaultValue() == null ? "unknown" : option.defaultValue().getClass().getSimpleName();
    }

    private ConfigSchemaExporter() {
    }
}
