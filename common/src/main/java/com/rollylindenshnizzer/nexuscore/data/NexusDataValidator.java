package com.rollylindenshnizzer.nexuscore.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class NexusDataValidator {
    public static DataValidationReport validatePlan(NexusData.DataPlan plan) {
        DataValidationReport report = new DataValidationReport();
        for (Map.Entry<String, String> translation : plan.translations().entrySet()) {
            if (translation.getValue().isBlank()) {
                report.warning(translation.getKey(), "Translation value is blank");
            }
        }
        for (String path : plan.assets().keySet()) {
            if (!path.endsWith(".json")) {
                report.error(path, "Generated asset path must end with .json");
            }
        }
        for (String path : plan.data().keySet()) {
            if (!path.endsWith(".json")) {
                report.error(path, "Generated data path must end with .json");
            }
        }
        return report;
    }

    public static DataValidationReport validateExisting(Path root, String... requiredRelativePaths) {
        DataValidationReport report = new DataValidationReport();
        for (String required : requiredRelativePaths) {
            if (!Files.exists(root.resolve(required))) {
                report.error(required, "Required generated file is missing");
            }
        }
        return report;
    }

    private NexusDataValidator() {
    }
}
