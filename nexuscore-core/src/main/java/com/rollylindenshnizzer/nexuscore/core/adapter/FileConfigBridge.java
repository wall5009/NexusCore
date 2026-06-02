package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.NexusPaths;
import com.rollylindenshnizzer.nexuscore.api.config.NexusConfig;
import com.rollylindenshnizzer.nexuscore.api.config.NexusConfigValue;
import com.rollylindenshnizzer.nexuscore.bridge.config.ConfigBridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FileConfigBridge implements ConfigBridge {
    private final NexusPaths paths;

    public FileConfigBridge(NexusPaths paths) {
        this.paths = paths;
    }

    @Override
    public Path configPath(String modId) {
        return paths.configDirectory().resolve(modId + ".json");
    }

    @Override
    public void loadOrCreate(NexusConfig config) {
        Path path = configPath(config.modId());
        if (!Files.exists(path)) {
            save(config);
            return;
        }
        try {
            String json = Files.readString(path);
            Map<String, String> parsed = parseFlatJson(json);
            for (NexusConfigValue<?> value : config.values().values()) {
                String raw = parsed.get(value.key());
                if (raw != null) {
                    assign(value, raw);
                }
            }
        } catch (IOException error) {
            throw new IllegalStateException("NexusCore could not load config '" + config.modId() + "'. Target config path: " + path + ". Reason: " + error.getMessage() + ". Fix: check file permissions or delete the invalid config to regenerate defaults.", error);
        }
    }

    @Override
    public void save(NexusConfig config) {
        Path path = configPath(config.modId());
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, toJson(config));
        } catch (IOException error) {
            throw new IllegalStateException("NexusCore could not save config '" + config.modId() + "'. Target config path: " + path + ". Reason: " + error.getMessage() + ". Fix: check file permissions for the config directory.", error);
        }
    }

    private String toJson(NexusConfig config) {
        StringBuilder builder = new StringBuilder("{\n");
        int index = 0;
        for (NexusConfigValue<?> value : config.values().values()) {
            if (index++ > 0) {
                builder.append(",\n");
            }
            builder.append("  \"").append(value.key()).append("\": ");
            Object current = value.get();
            if (current instanceof String string) {
                builder.append('"').append(string.replace("\"", "\\\"")).append('"');
            } else {
                builder.append(current);
            }
        }
        return builder.append("\n}\n").toString();
    }

    private Map<String, String> parseFlatJson(String json) {
        Map<String, String> values = new LinkedHashMap<>();
        String trimmed = json.replace("{", "").replace("}", "");
        for (String part : trimmed.split(",")) {
            String[] pieces = part.split(":", 2);
            if (pieces.length == 2) {
                String key = clean(pieces[0]);
                String value = clean(pieces[1]);
                values.put(key, value);
            }
        }
        return values;
    }

    private String clean(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void assign(NexusConfigValue value, String raw) {
        if (value.type() == Boolean.class) {
            value.set(Boolean.parseBoolean(raw));
        } else if (value.type() == Integer.class) {
            int parsed = Integer.parseInt(raw);
            Integer min = (Integer) value.min();
            Integer max = (Integer) value.max();
            if (min != null && parsed < min) {
                parsed = min;
            }
            if (max != null && parsed > max) {
                parsed = max;
            }
            value.set(parsed);
        } else {
            value.set(raw);
        }
    }
}
