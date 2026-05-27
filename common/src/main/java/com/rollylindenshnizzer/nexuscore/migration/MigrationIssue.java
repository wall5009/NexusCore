package com.rollylindenshnizzer.nexuscore.migration;

public record MigrationIssue(MigrationSeverity severity, String code, String subject, String message, String replacementHint) {
}
