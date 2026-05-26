package com.rollylindenshnizzer.nexuscore.debug;

import com.rollylindenshnizzer.nexuscore.core.NexusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class ReportExporter {
    public static void write(Path path, Map<String, String> sections) {
        StringBuilder builder = new StringBuilder();
        sections.forEach((key, value) -> builder.append("## ").append(key).append(System.lineSeparator())
                .append(value).append(System.lineSeparator()).append(System.lineSeparator()));
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, builder.toString());
        } catch (IOException exception) {
            throw new NexusException("Failed to write report " + path, exception);
        }
    }

    private ReportExporter() {
    }
}
