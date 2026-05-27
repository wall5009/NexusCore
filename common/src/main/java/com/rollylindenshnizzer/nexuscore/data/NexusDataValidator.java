package com.rollylindenshnizzer.nexuscore.data;

import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class NexusDataValidator {
    public static DataValidationReport validatePlan(NexusData.DataPlan plan) {
        DataValidationReport report = new DataValidationReport();
        for (Map.Entry<String, String> translation : plan.translations().entrySet()) {
            if (translation.getValue().isBlank()) {
                report.warning(translation.getKey(), "Translation value is blank", "Provide a readable English fallback.");
            }
            if (translation.getKey().contains("%")) {
                report.warning(translation.getKey(), "Translation key contains placeholder characters", "Place placeholders in the value, not the key.");
            }
        }
        Set<String> paths = new HashSet<>();
        for (String path : plan.assets().keySet()) {
            if (!paths.add("asset:" + path)) {
                report.error(path, "Duplicate generated asset path", "Generate this file from only one helper.");
            }
            if (!path.endsWith(".json")) {
                report.error(path, "Generated asset path must end with .json", "Use a JSON-relative path under assets/<modid>.");
            }
            validateAsset(path, plan.assets().get(path), report);
        }
        for (String path : plan.data().keySet()) {
            if (!paths.add("data:" + path)) {
                report.error(path, "Duplicate generated data path", "Generate this file from only one helper.");
            }
            if (!path.endsWith(".json")) {
                report.error(path, "Generated data path must end with .json", "Use a JSON-relative path under data/<modid>.");
            }
            validateData(path, plan.data().get(path), report);
        }
        return report;
    }

    public static DataValidationReport validateExisting(Path root, String... requiredRelativePaths) {
        DataValidationReport report = new DataValidationReport();
        for (String required : requiredRelativePaths) {
            if (!Files.exists(root.resolve(required))) {
                report.error(required, "Required generated file is missing", "Run datagen or add the file manually.");
            }
        }
        return report;
    }

    private static void validateAsset(String path, JsonObject json, DataValidationReport report) {
        if (path.startsWith("models/") && !json.has("parent")) {
            report.warning(path, "Generated model has no parent", "Set a parent model such as minecraft:item/generated.");
        }
        if (path.startsWith("blockstates/") && !json.has("variants") && !json.has("multipart")) {
            report.error(path, "Blockstate has neither variants nor multipart", "Add one of the vanilla blockstate roots.");
        }
    }

    private static void validateData(String path, JsonObject json, DataValidationReport report) {
        if (path.startsWith("recipe/") || path.startsWith("recipes/")) {
            if (!json.has("criterion") && !json.has("criteria")) {
                report.warning(path, "Recipe has no unlock criteria", "Add at least one unlock criterion for survival recipes.");
            }
            if (!json.has("result")) {
                report.warning(path, "Recipe has no result field", "Check that the recipe builder produced the expected output.");
            }
        }
        if (path.startsWith("tags/") && !json.has("values")) {
            report.error(path, "Tag has no values array", "Add values or remove the empty tag file.");
        }
        if (path.startsWith("loot_table/") && !json.has("pools")) {
            report.warning(path, "Loot table has no pools", "Add at least one loot pool or confirm this is intentionally empty.");
        }
    }

    private NexusDataValidator() {
    }
}
