package com.rollylindenshnizzer.nexuscore.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public final class RecipeDiagnostics {
    public static DataValidationReport validate(Map<String, JsonObject> recipes) {
        DataValidationReport report = new DataValidationReport();
        Map<String, String> outputs = new HashMap<>();
        Map<String, String> signatures = new HashMap<>();
        for (Map.Entry<String, JsonObject> entry : recipes.entrySet()) {
            String path = entry.getKey();
            JsonObject recipe = entry.getValue();
            String output = output(recipe);
            if (!output.isBlank()) {
                String previous = outputs.putIfAbsent(output, path);
                if (previous != null) {
                    report.warning(path, "Duplicate recipe output " + output, "Check conflict with " + previous);
                }
            }
            String signature = recipeSignature(recipe);
            String previous = signatures.putIfAbsent(signature, path);
            if (!signature.isBlank() && previous != null) {
                report.warning(path, "Duplicate recipe input pattern", "Check conflict with " + previous);
            }
            if (!recipe.has("criterion") && !recipe.has("criteria")) {
                report.warning(path, "Recipe has no unlock criterion", "Add advancement criteria.");
            }
        }
        return report;
    }

    private static String output(JsonObject recipe) {
        JsonElement result = recipe.get("result");
        if (result == null) {
            return "";
        }
        if (result.isJsonPrimitive()) {
            return result.getAsString();
        }
        JsonObject object = result.getAsJsonObject();
        if (object.has("id")) {
            return object.get("id").getAsString();
        }
        if (object.has("item")) {
            return object.get("item").getAsString();
        }
        return "";
    }

    private static String recipeSignature(JsonObject recipe) {
        if (recipe.has("pattern")) {
            return recipe.get("pattern").toString() + recipe.get("key");
        }
        if (recipe.has("ingredients")) {
            return recipe.get("ingredients").toString();
        }
        return "";
    }

    private RecipeDiagnostics() {
    }
}
