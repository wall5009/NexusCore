package com.rollylindenshnizzer.nexuscore.config;

public final class EnumOption<E extends Enum<E>> extends ConfigOption<E> {
    private final Class<E> enumClass;

    EnumOption(String key, E defaultValue, Class<E> enumClass) {
        super(key, defaultValue);
        this.enumClass = enumClass;
    }

    public Class<E> enumClass() {
        return enumClass;
    }
}
