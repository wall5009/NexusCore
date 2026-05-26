package com.rollylindenshnizzer.nexuscore.config;

import java.util.regex.Pattern;

public final class StringOption extends ConfigOption<String> {
    StringOption(String key, String defaultValue) {
        super(key, defaultValue);
    }

    public StringOption regex(String regex) {
        Pattern pattern = Pattern.compile(regex);
        validator(value -> pattern.matcher(value).matches(), "must match " + regex);
        return this;
    }

    public StringOption notBlank() {
        validator(value -> value != null && !value.isBlank(), "must not be blank");
        return this;
    }
}
