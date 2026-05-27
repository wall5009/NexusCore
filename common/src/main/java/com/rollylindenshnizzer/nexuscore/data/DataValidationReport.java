package com.rollylindenshnizzer.nexuscore.data;

import java.util.ArrayList;
import java.util.List;

public final class DataValidationReport {
    private final List<Issue> issues = new ArrayList<>();

    public DataValidationReport error(String path, String message) {
        issues.add(new Issue(Severity.ERROR, path, message));
        return this;
    }

    public DataValidationReport error(String path, String message, String suggestion) {
        issues.add(new Issue(Severity.ERROR, path, message, suggestion));
        return this;
    }

    public DataValidationReport warning(String path, String message) {
        issues.add(new Issue(Severity.WARNING, path, message));
        return this;
    }

    public DataValidationReport warning(String path, String message, String suggestion) {
        issues.add(new Issue(Severity.WARNING, path, message, suggestion));
        return this;
    }

    public DataValidationReport merge(DataValidationReport other) {
        issues.addAll(other.issues);
        return this;
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(issue -> issue.severity() == Severity.ERROR);
    }

    public List<Issue> issues() {
        return List.copyOf(issues);
    }

    public String summary() {
        long errors = issues.stream().filter(issue -> issue.severity() == Severity.ERROR).count();
        long warnings = issues.stream().filter(issue -> issue.severity() == Severity.WARNING).count();
        return "Data validation: " + errors + " errors, " + warnings + " warnings";
    }

    public enum Severity {
        WARNING,
        ERROR
    }

    public record Issue(Severity severity, String path, String message, String suggestion) {
        public Issue(Severity severity, String path, String message) {
            this(severity, path, message, "");
        }
    }
}
