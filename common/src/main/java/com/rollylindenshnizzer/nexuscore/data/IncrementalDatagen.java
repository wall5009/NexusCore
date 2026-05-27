package com.rollylindenshnizzer.nexuscore.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.core.NexusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class IncrementalDatagen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Result writeIfChanged(Path root, Map<String, String> files, boolean force) {
        Map<String, String> previous = readCache(root.resolve(".nexus-datagen-cache.json"));
        Map<String, String> next = new LinkedHashMap<>();
        int written = 0;
        int skipped = 0;
        try {
            for (Map.Entry<String, String> file : files.entrySet()) {
                String hash = sha256(file.getValue());
                next.put(file.getKey(), hash);
                Path path = root.resolve(file.getKey()).normalize();
                if (!force && hash.equals(previous.get(file.getKey())) && Files.exists(path)) {
                    skipped++;
                    continue;
                }
                Files.createDirectories(path.getParent());
                Files.writeString(path, file.getValue());
                written++;
            }
            writeCache(root.resolve(".nexus-datagen-cache.json"), next);
            return new Result(written, skipped, files.size());
        } catch (IOException exception) {
            throw new NexusException("Failed incremental datagen write", exception);
        }
    }

    private static Map<String, String> readCache(Path path) {
        if (!Files.exists(path)) {
            return Map.of();
        }
        try {
            JsonObject json = GSON.fromJson(Files.readString(path), JsonObject.class);
            Map<String, String> values = new LinkedHashMap<>();
            for (String key : json.keySet()) {
                values.put(key, json.get(key).getAsString());
            }
            return values;
        } catch (IOException exception) {
            throw new NexusException("Failed to read datagen cache " + path, exception);
        }
    }

    private static void writeCache(Path path, Map<String, String> cache) throws IOException {
        JsonObject json = new JsonObject();
        cache.forEach(json::addProperty);
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(json));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new NexusException("SHA-256 is unavailable", exception);
        }
    }

    public record Result(int written, int skipped, int total) {
        public String summary() {
            return "Incremental datagen wrote " + written + ", skipped " + skipped + " unchanged files out of " + total;
        }
    }

    private IncrementalDatagen() {
    }
}
