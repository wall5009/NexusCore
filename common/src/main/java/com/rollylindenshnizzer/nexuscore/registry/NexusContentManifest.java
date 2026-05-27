package com.rollylindenshnizzer.nexuscore.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.config.NexusConfigRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NexusContentManifest {
    private static final Map<String, List<Entry>> ENTRIES = new ConcurrentHashMap<>();

    public static void record(String modId, String group, String type, String id, String source, String owner) {
        ENTRIES.computeIfAbsent(modId, ignored -> new ArrayList<>())
                .add(new Entry(group, type, id, source == null ? "" : source, owner == null ? "" : owner));
    }

    public static void recordGenerated(String modId, String type, String relativePath) {
        record(modId, "generated", type, relativePath, sourceHint(), "datagen");
    }

    public static List<Entry> entries(String modId) {
        return ENTRIES.getOrDefault(modId, List.of()).stream()
                .sorted(Comparator.comparing(Entry::type).thenComparing(Entry::id))
                .toList();
    }

    public static JsonObject json(String modId) {
        JsonObject root = new JsonObject();
        root.addProperty("modId", modId);
        JsonArray entries = new JsonArray();
        for (Entry entry : entries(modId)) {
            JsonObject json = new JsonObject();
            json.addProperty("group", entry.group());
            json.addProperty("type", entry.type());
            json.addProperty("id", entry.id());
            json.addProperty("source", entry.source());
            json.addProperty("owner", entry.owner());
            entries.add(json);
        }
        root.add("entries", entries);

        JsonArray configs = new JsonArray();
        NexusConfigRegistry.configs().stream()
                .filter(config -> config.modId().equals(modId))
                .forEach(config -> configs.add(config.modId()));
        root.add("configFiles", configs);
        return root;
    }

    public static Map<String, List<Entry>> all() {
        return Map.copyOf(ENTRIES);
    }

    public static String sourceHint() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames
                .filter(frame -> !frame.getClassName().startsWith("com.rollylindenshnizzer.nexuscore.registry.NexusContentManifest"))
                .filter(frame -> !frame.getClassName().startsWith("java."))
                .findFirst()
                .map(frame -> frame.getClassName() + ":" + frame.getLineNumber())
                .orElse(""));
    }

    public record Entry(String group, String type, String id, String source, String owner) {
    }

    private NexusContentManifest() {
    }
}
