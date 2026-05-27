package com.rollylindenshnizzer.nexuscore.config;

import java.util.LinkedHashMap;
import java.util.Map;

public record ConfigPreset(String id, String description, String icon, Map<String, Object> values) {
    public ConfigPreset(String id, Map<String, Object> values) {
        this(id, "", "", values);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final String id;
        private String description = "";
        private String icon = "";
        private final Map<String, Object> values = new LinkedHashMap<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder description(String description) {
            this.description = description == null ? "" : description;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon == null ? "" : icon;
            return this;
        }

        public Builder value(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public ConfigPreset build() {
            return new ConfigPreset(id, description, icon, Map.copyOf(values));
        }
    }
}
