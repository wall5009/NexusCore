package com.rollylindenshnizzer.nexuscore.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JsonSchema {
    private final List<Field> fields = new ArrayList<>();

    public JsonSchema require(String name, Type type) {
        fields.add(new Field(name, type, true));
        return this;
    }

    public JsonSchema optional(String name, Type type) {
        fields.add(new Field(name, type, false));
        return this;
    }

    public List<String> validate(JsonObject object) {
        List<String> errors = new ArrayList<>();
        for (Field field : fields) {
            JsonElement value = object.get(field.name());
            if (value == null) {
                if (field.required()) {
                    errors.add("Missing required field " + field.name());
                }
                continue;
            }
            if (!field.type().matches(value)) {
                errors.add("Field " + field.name() + " must be " + field.type());
            }
        }
        return errors;
    }

    public enum Type {
        STRING {
            @Override
            boolean matches(JsonElement value) {
                return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
            }
        },
        NUMBER {
            @Override
            boolean matches(JsonElement value) {
                return value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber();
            }
        },
        BOOLEAN {
            @Override
            boolean matches(JsonElement value) {
                return value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
            }
        },
        OBJECT {
            @Override
            boolean matches(JsonElement value) {
                return value.isJsonObject();
            }
        },
        ARRAY {
            @Override
            boolean matches(JsonElement value) {
                return value.isJsonArray();
            }
        };

        abstract boolean matches(JsonElement value);
    }

    private record Field(String name, Type type, boolean required) {
    }
}
