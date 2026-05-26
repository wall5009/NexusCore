package com.rollylindenshnizzer.nexuscore.config;

import com.rollylindenshnizzer.nexuscore.core.InvalidConfigException;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class NexusConfig {
    private final String modId;
    private final Map<String, ConfigOption<?>> options = new LinkedHashMap<>();

    protected NexusConfig(String modId) {
        this.modId = NexusIds.requireNamespace(modId);
        NexusConfigRegistry.register(this);
    }

    public String modId() {
        return modId;
    }

    public IntOption intOption(String key, int defaultValue) {
        return add(new IntOption(key, defaultValue));
    }

    public BooleanOption booleanOption(String key, boolean defaultValue) {
        return add(new BooleanOption(key, defaultValue));
    }

    public StringOption stringOption(String key, String defaultValue) {
        return add(new StringOption(key, defaultValue));
    }

    public <E extends Enum<E>> EnumOption<E> enumOption(String key, E defaultValue, Class<E> enumClass) {
        return add(new EnumOption<>(key, defaultValue, enumClass));
    }

    public Map<String, ConfigOption<?>> options() {
        return Collections.unmodifiableMap(options);
    }

    public void validateAll() {
        for (ConfigOption<?> option : options.values()) {
            option.validateCurrent();
        }
    }

    @SuppressWarnings("unchecked")
    private <O extends ConfigOption<?>> O add(O option) {
        if (options.put(option.key(), option) != null) {
            throw new InvalidConfigException(option.key(), "duplicate option key");
        }
        return option;
    }
}
