package com.rollylindenshnizzer.nexuscore.config;

import java.util.LinkedHashMap;
import java.util.Map;

public record ConfigPreset(String id, Map<String, Object> values) {
    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final String id;
        private final Map<String, Object> values = new LinkedHashMap<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder value(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public ConfigPreset build() {
            return new ConfigPreset(id, Map.copyOf(values));
        }
    }
}
