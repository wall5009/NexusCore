package com.rollylindenshnizzer.nexuscore.config;

import com.rollylindenshnizzer.nexuscore.core.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ConfigOption<T> {
    private final String key;
    private final T defaultValue;
    private final List<Rule<T>> validators = new ArrayList<>();
    private T value;
    private boolean restartRequired;
    private boolean worldReloadRequired;
    private boolean serverSynced;
    private boolean visible = true;
    private boolean enabled = true;
    private String comment = "";
    private String group = "general";
    private String translationKey = "";
    private final List<String> dependencies = new ArrayList<>();
    private final List<String> conflicts = new ArrayList<>();

    protected ConfigOption(String key, T defaultValue) {
        this.key = Objects.requireNonNull(key, "key");
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String key() {
        return key;
    }

    public T get() {
        return value;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public ConfigOption<T> set(T value) {
        validate(value);
        this.value = value;
        return this;
    }

    public ConfigOption<T> reset() {
        return set(defaultValue);
    }

    public ConfigOption<T> validator(Predicate<T> predicate, String message) {
        validators.add(new Rule<>(predicate, message));
        return this;
    }

    public ConfigOption<T> comment(String comment) {
        this.comment = comment;
        return this;
    }

    public ConfigOption<T> group(String group) {
        this.group = group == null || group.isBlank() ? "general" : group;
        return this;
    }

    public ConfigOption<T> translationKey(String translationKey) {
        this.translationKey = translationKey == null ? "" : translationKey;
        return this;
    }

    public ConfigOption<T> requiresRestart() {
        this.restartRequired = true;
        return this;
    }

    public ConfigOption<T> requiresWorldReload() {
        this.worldReloadRequired = true;
        return this;
    }

    public ConfigOption<T> serverSynced() {
        this.serverSynced = true;
        return this;
    }

    public ConfigOption<T> visibleWhen(boolean visible) {
        this.visible = visible;
        return this;
    }

    public ConfigOption<T> enabledWhen(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ConfigOption<T> dependsOn(ConfigOption<?> option) {
        dependencies.add(option.key());
        return this;
    }

    public ConfigOption<T> conflictsWith(ConfigOption<?> option) {
        conflicts.add(option.key());
        return this;
    }

    public boolean restartRequired() {
        return restartRequired;
    }

    public boolean worldReloadRequired() {
        return worldReloadRequired;
    }

    public boolean isServerSynced() {
        return serverSynced;
    }

    public boolean visible() {
        return visible;
    }

    public boolean enabled() {
        return enabled;
    }

    public String comment() {
        return comment;
    }

    public String group() {
        return group;
    }

    public String translationKey() {
        return translationKey;
    }

    public List<String> dependencies() {
        return List.copyOf(dependencies);
    }

    public List<String> conflicts() {
        return List.copyOf(conflicts);
    }

    void validateCurrent() {
        validate(value);
    }

    protected void validate(T candidate) {
        for (Rule<T> validator : validators) {
            if (!validator.predicate().test(candidate)) {
                throw new InvalidConfigException(key, validator.message());
            }
        }
    }

    private record Rule<T>(Predicate<T> predicate, String message) {
    }
}
