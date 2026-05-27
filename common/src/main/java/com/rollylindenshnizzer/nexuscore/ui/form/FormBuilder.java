package com.rollylindenshnizzer.nexuscore.ui.form;

import com.rollylindenshnizzer.nexuscore.config.ConfigOption;
import com.rollylindenshnizzer.nexuscore.config.NexusConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class FormBuilder {
    private final Map<String, FormField<?>> fields = new LinkedHashMap<>();
    private Runnable resetCallback = () -> {};
    private Consumer<Map<String, Object>> submitCallback = ignored -> {};

    public static FormBuilder create() {
        return new FormBuilder();
    }

    public <T> FormField<T> field(String key, T defaultValue) {
        FormField<T> field = new FormField<>(key, defaultValue);
        fields.put(key, field);
        return field;
    }

    public FormBuilder bind(NexusConfig config) {
        for (ConfigOption<?> option : config.options().values()) {
            fields.put(option.key(), new FormField<>(option.key(), option.get()));
        }
        return this;
    }

    public FormBuilder onSubmit(Consumer<Map<String, Object>> submitCallback) {
        this.submitCallback = submitCallback;
        return this;
    }

    public FormBuilder onReset(Runnable resetCallback) {
        this.resetCallback = resetCallback;
        return this;
    }

    public Result submit() {
        List<String> errors = fields.values().stream().flatMap(field -> field.validate().stream()).toList();
        if (!errors.isEmpty()) {
            return new Result(false, errors);
        }
        submitCallback.accept(values());
        return new Result(true, List.of());
    }

    public void reset() {
        fields.values().forEach(FormField::reset);
        resetCallback.run();
    }

    public boolean dirty() {
        return fields.values().stream().anyMatch(FormField::dirty);
    }

    public Map<String, Object> values() {
        Map<String, Object> values = new LinkedHashMap<>();
        fields.forEach((key, field) -> values.put(key, field.value()));
        return values;
    }

    public record Result(boolean successful, List<String> errors) {
    }
}
