package com.rollylindenshnizzer.nexuscore.debug;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class DoctorReport {
    private final List<DoctorIssue> issues = new ArrayList<>();

    public DoctorReport issue(DoctorSeverity severity, String code, String title, String detail, String suggestion) {
        issues.add(new DoctorIssue(severity, code, title, detail, suggestion));
        return this;
    }

    public DoctorReport info(String code, String title, String detail) {
        return issue(DoctorSeverity.INFO, code, title, detail, "");
    }

    public DoctorReport warning(String code, String title, String detail, String suggestion) {
        return issue(DoctorSeverity.WARNING, code, title, detail, suggestion);
    }

    public DoctorReport error(String code, String title, String detail, String suggestion) {
        return issue(DoctorSeverity.ERROR, code, title, detail, suggestion);
    }

    public List<DoctorIssue> issues() {
        return List.copyOf(issues);
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(issue -> issue.severity() == DoctorSeverity.ERROR);
    }

    public String summary() {
        long errors = issues.stream().filter(issue -> issue.severity() == DoctorSeverity.ERROR).count();
        long warnings = issues.stream().filter(issue -> issue.severity() == DoctorSeverity.WARNING).count();
        return "Nexus Doctor: " + errors + " errors, " + warnings + " warnings, " + issues.size() + " total findings";
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("summary", summary());
        JsonArray array = new JsonArray();
        for (DoctorIssue issue : issues) {
            JsonObject json = new JsonObject();
            json.addProperty("severity", issue.severity().name());
            json.addProperty("code", issue.code());
            json.addProperty("title", issue.title());
            json.addProperty("detail", issue.detail());
            json.addProperty("suggestion", issue.suggestion());
            array.add(json);
        }
        root.add("issues", array);
        return root;
    }

    public String toMarkdown() {
        StringBuilder builder = new StringBuilder("# Nexus Doctor Report\n\n");
        builder.append(summary()).append("\n\n");
        builder.append("| Severity | Code | Title | Detail | Suggestion |\n");
        builder.append("| --- | --- | --- | --- | --- |\n");
        for (DoctorIssue issue : issues) {
            builder.append("| ").append(issue.severity())
                    .append(" | `").append(issue.code()).append("`")
                    .append(" | ").append(issue.title())
                    .append(" | ").append(issue.detail())
                    .append(" | ").append(issue.suggestion())
                    .append(" |\n");
        }
        return builder.toString();
    }
}
