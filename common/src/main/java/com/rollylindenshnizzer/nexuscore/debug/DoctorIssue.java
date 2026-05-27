package com.rollylindenshnizzer.nexuscore.debug;

public record DoctorIssue(DoctorSeverity severity, String code, String title, String detail, String suggestion) {
}
