package com.rollylindenshnizzer.nexuscore.api.config;

import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class NexusConfig {
    private final String modId;
    private final Map<String, NexusConfigValue<?>> values;

    private NexusConfig(String modId, Map<String, NexusConfigValue<?>> values) {
        this.modId = modId;
        this.values = new LinkedHashMap<>(values);
    }

    public static Builder create(String modId) {
        return new Builder(modId);
    }

    public String modId() {
        return modId;
    }

    public Map<String, NexusConfigValue<?>> values() {
        return new LinkedHashMap<>(values);
    }

    public boolean getBoolean(String key) {
        return (Boolean) value(key).get();
    }

    public int getInt(String key) {
        return (Integer) value(key).get();
    }

    public String getString(String key) {
        return (String) value(key).get();
    }

    public void save() {
        NexusServices.get().config().save(this);
    }

    public void load() {
        NexusServices.get().config().loadOrCreate(this);
    }

    private NexusConfigValue<?> value(String key) {
        NexusConfigValue<?> value = values.get(key);
        if (value == null) {
            throw new IllegalArgumentException("NexusCore config '" + modId + "' does not contain key '" + key + "'.");
        }
        return value;
    }

    public static final class Builder {
        private final String modId;
        private final Map<String, NexusConfigValue<?>> values = new LinkedHashMap<>();

        private Builder(String modId) {
            this.modId = Objects.requireNonNull(modId, "modId");
        }

        public Builder booleanValue(String key, boolean defaultValue) {
            values.put(key, new NexusConfigValue<>(key, Boolean.class, defaultValue, null, null));
            return this;
        }

        public Builder intValue(String key, int defaultValue, int min, int max) {
            if (min > max) {
                throw new IllegalArgumentException("NexusCore config key '" + key + "' has min greater than max.");
            }
            if (defaultValue < min || defaultValue > max) {
                throw new IllegalArgumentException("NexusCore config key '" + key + "' default " + defaultValue + " is outside " + min + ".." + max + ".");
            }
            values.put(key, new NexusConfigValue<>(key, Integer.class, defaultValue, min, max));
            return this;
        }

        public Builder stringValue(String key, String defaultValue) {
            values.put(key, new NexusConfigValue<>(key, String.class, defaultValue, null, null));
            return this;
        }

        public NexusConfig build() {
            NexusConfig config = new NexusConfig(modId, values);
            if (NexusServices.isInstalled()) {
                NexusServices.get().config().loadOrCreate(config);
            }
            return config;
        }
    }
}
