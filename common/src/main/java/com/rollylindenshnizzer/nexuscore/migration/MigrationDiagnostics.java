package com.rollylindenshnizzer.nexuscore.migration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class MigrationDiagnostics {
    private final List<MigrationIssue> issues = new ArrayList<>();

    public MigrationDiagnostics renamedId(String oldId, String newId) {
        return issue(MigrationSeverity.WARNING, "NC-MIG-RENAMED-ID", oldId,
                "Identifier was renamed", "Use " + newId);
    }

    public MigrationDiagnostics removedConfigKey(String key, String replacement) {
        return issue(MigrationSeverity.WARNING, "NC-MIG-REMOVED-CONFIG", key,
                "Config key is no longer used", replacement == null || replacement.isBlank() ? "Remove this key" : "Use " + replacement);
    }

    public MigrationDiagnostics oldGeneratedFormat(String path, String replacement) {
        return issue(MigrationSeverity.WARNING, "NC-MIG-GENERATED-FORMAT", path,
                "Generated file format changed in v1.1", "Regenerate using " + replacement);
    }

    public MigrationDiagnostics issue(MigrationSeverity severity, String code, String subject, String message,
                                      String replacementHint) {
        issues.add(new MigrationIssue(severity, code, subject, message, replacementHint));
        return this;
    }

    public List<MigrationIssue> issues() {
        return List.copyOf(issues);
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonArray array = new JsonArray();
        for (MigrationIssue issue : issues) {
            JsonObject json = new JsonObject();
            json.addProperty("severity", issue.severity().name());
            json.addProperty("code", issue.code());
            json.addProperty("subject", issue.subject());
            json.addProperty("message", issue.message());
            json.addProperty("replacementHint", issue.replacementHint());
            array.add(json);
        }
        root.add("issues", array);
        return root;
    }
}
