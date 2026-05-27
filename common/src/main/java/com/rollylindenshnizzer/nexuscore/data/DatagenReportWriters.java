package com.rollylindenshnizzer.nexuscore.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.core.NexusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DatagenReportWriters {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void writeJson(DataValidationReport report, Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(toJson(report)));
        } catch (IOException exception) {
            throw new NexusException("Failed to write datagen JSON report " + path, exception);
        }
    }

    public static void writeMarkdown(DataValidationReport report, Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, toMarkdown(report));
        } catch (IOException exception) {
            throw new NexusException("Failed to write datagen Markdown report " + path, exception);
        }
    }

    public static void writeHtml(DataValidationReport report, Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, "<!doctype html><meta charset=\"utf-8\"><title>Nexus Datagen Report</title><pre>"
                    + escape(toMarkdown(report)) + "</pre>");
        } catch (IOException exception) {
            throw new NexusException("Failed to write datagen HTML report " + path, exception);
        }
    }

    public static JsonObject toJson(DataValidationReport report) {
        JsonObject root = new JsonObject();
        root.addProperty("summary", report.summary());
        root.addProperty("hasErrors", report.hasErrors());
        JsonArray issues = new JsonArray();
        for (DataValidationReport.Issue issue : report.issues()) {
            JsonObject json = new JsonObject();
            json.addProperty("severity", issue.severity().name());
            json.addProperty("path", issue.path());
            json.addProperty("message", issue.message());
            json.addProperty("suggestion", issue.suggestion());
            issues.add(json);
        }
        root.add("issues", issues);
        return root;
    }

    public static String toMarkdown(DataValidationReport report) {
        StringBuilder builder = new StringBuilder("# Nexus Datagen Validation\n\n");
        builder.append(report.summary()).append("\n\n");
        builder.append("| Severity | Path | Message | Suggestion |\n");
        builder.append("| --- | --- | --- | --- |\n");
        for (DataValidationReport.Issue issue : report.issues()) {
            builder.append("| ").append(issue.severity())
                    .append(" | `").append(issue.path()).append("`")
                    .append(" | ").append(issue.message())
                    .append(" | ").append(issue.suggestion())
                    .append(" |\n");
        }
        return builder.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private DatagenReportWriters() {
    }
}
