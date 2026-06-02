package com.rollylindenshnizzer.nexuscore.api.config;

public final class NexusConfigValue<T> {
    private final String key;
    private final Class<T> type;
    private final T defaultValue;
    private final T min;
    private final T max;
    private T value;

    public NexusConfigValue(String key, Class<T> type, T defaultValue, T min, T max) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.value = defaultValue;
    }

    public String key() {
        return key;
    }

    public Class<T> type() {
        return type;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public T min() {
        return min;
    }

    public T max() {
        return max;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
