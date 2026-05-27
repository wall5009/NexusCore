package com.rollylindenshnizzer.nexuscore.worldgen;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.util.ArrayList;
import java.util.List;

@NexusStable(since = "1.2")
public record WorldgenValidationReport(List<String> errors, List<String> warnings) {
    public WorldgenValidationReport {
        errors = errors == null ? List.of() : List.copyOf(errors);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public boolean passed() {
        return errors.isEmpty();
    }

    public static WorldgenValidationReport ore(String path, int veinSize, int minY, int maxY) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (path == null || path.isBlank()) {
            errors.add("Ore path is blank");
        }
        if (veinSize <= 0) {
            errors.add("Ore vein size must be positive");
        }
        if (minY > maxY) {
            errors.add("Ore height range is inverted");
        }
        if (veinSize > 64) {
            warnings.add("Large ore vein size may be expensive during chunk generation");
        }
        return new WorldgenValidationReport(errors, warnings);
    }
}
