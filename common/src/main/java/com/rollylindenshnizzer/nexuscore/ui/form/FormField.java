package com.rollylindenshnizzer.nexuscore.ui.form;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class FormField<T> {
    private final String key;
    private T value;
    private final T defaultValue;
    private boolean enabled = true;
    private final List<Rule<T>> validators = new ArrayList<>();

    FormField(String key, T defaultValue) {
        this.key = key;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public String key() {
        return key;
    }

    public T value() {
        return value;
    }

    public FormField<T> value(T value) {
        this.value = value;
        return this;
    }

    public FormField<T> enabledWhen(Supplier<Boolean> condition) {
        this.enabled = condition.get();
        return this;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean dirty() {
        return !java.util.Objects.equals(value, defaultValue);
    }

    public FormField<T> reset() {
        value = defaultValue;
        return this;
    }

    public FormField<T> validator(Predicate<T> predicate, String message) {
        validators.add(new Rule<>(predicate, message));
        return this;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        for (Rule<T> rule : validators) {
            if (!rule.predicate().test(value)) {
                errors.add(rule.message());
            }
        }
        return errors;
    }

    private record Rule<T>(Predicate<T> predicate, String message) {
    }
}
