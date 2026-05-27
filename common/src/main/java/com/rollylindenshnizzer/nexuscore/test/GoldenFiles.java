package com.rollylindenshnizzer.nexuscore.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.rollylindenshnizzer.nexuscore.core.NexusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GoldenFiles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void assertJsonEquals(Path expected, Path actual) {
        try {
            String left = normalizeJson(Files.readString(expected));
            String right = normalizeJson(Files.readString(actual));
            if (!left.equals(right)) {
                throw new AssertionError("Golden JSON mismatch\nExpected: " + expected + "\nActual: " + actual);
            }
        } catch (IOException exception) {
            throw new NexusException("Failed to compare golden files", exception);
        }
    }

    public static void update(Path golden, String content) {
        try {
            Files.createDirectories(golden.getParent());
            Files.writeString(golden, content);
        } catch (IOException exception) {
            throw new NexusException("Failed to update golden file " + golden, exception);
        }
    }

    public static String normalizeJson(String json) {
        return GSON.toJson(JsonParser.parseString(json));
    }

    private GoldenFiles() {
    }
}
