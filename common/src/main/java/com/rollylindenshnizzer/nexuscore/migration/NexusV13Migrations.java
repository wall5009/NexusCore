package com.rollylindenshnizzer.nexuscore.migration;

import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.util.ArrayList;
import java.util.List;

@NexusStable(since = "1.3")
public final class NexusV13Migrations {
    public static MigrationResult migrateMachinePreview(JsonObject oldJson) {
        JsonObject next = oldJson.deepCopy();
        List<String> warnings = new ArrayList<>();
        if (next.has("energyCost")) {
            next.add("energy", next.remove("energyCost"));
            warnings.add("Renamed energyCost to energy");
        }
        if (next.has("processTicks")) {
            next.add("ticks", next.remove("processTicks"));
            warnings.add("Renamed processTicks to ticks");
        }
        next.addProperty("schema_version", "1.3");
        return new MigrationResult(next, warnings);
    }

    public static MigrationResult migrateWorldgenHelper(JsonObject oldJson) {
        JsonObject next = oldJson.deepCopy();
        List<String> warnings = new ArrayList<>();
        if (next.has("height")) {
            next.add("height_range", next.remove("height"));
            warnings.add("Renamed height to height_range");
        }
        next.addProperty("schema_version", "1.3");
        return new MigrationResult(next, warnings);
    }

    public static MigrationResult migrateEntityGoal(JsonObject oldJson) {
        JsonObject next = oldJson.deepCopy();
        List<String> warnings = new ArrayList<>();
        if (next.has("flags")) {
            next.add("controls", next.remove("flags"));
            warnings.add("Renamed flags to controls");
        }
        next.addProperty("schema_version", "1.3");
        return new MigrationResult(next, warnings);
    }

    public static MigrationResult migrateBalanceProfile(JsonObject oldJson) {
        JsonObject next = oldJson.deepCopy();
        List<String> warnings = new ArrayList<>();
        if (!next.has("profile_version")) {
            next.addProperty("profile_version", "1.3");
            warnings.add("Added profile_version 1.3");
        }
        return new MigrationResult(next, warnings);
    }

    @NexusStable(since = "1.3")
    public record MigrationResult(JsonObject json, List<String> warnings) {
        public MigrationResult {
            json = json.deepCopy();
            warnings = List.copyOf(warnings);
        }

        public boolean changed() {
            return !warnings.isEmpty();
        }
    }

    private NexusV13Migrations() {
    }
}
