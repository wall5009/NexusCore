package com.rollylindenshnizzer.nexuscore.compat;

import com.rollylindenshnizzer.nexuscore.core.NexusEnvironment;

import java.util.ArrayList;
import java.util.List;

public final class CompatModuleLoader {
    private final List<Result> results = new ArrayList<>();

    public void load(CompatModule module) {
        List<String> missing = module.requiredMods().stream().filter(mod -> !NexusEnvironment.isModLoaded(mod)).toList();
        if (!missing.isEmpty()) {
            results.add(new Result(module.id(), false, "Missing optional mods: " + missing));
            return;
        }
        if (!module.enabled()) {
            results.add(new Result(module.id(), false, "Disabled by config"));
            return;
        }
        try {
            module.initialize();
            results.add(new Result(module.id(), true, "Loaded"));
        } catch (RuntimeException exception) {
            results.add(new Result(module.id(), false, exception.getMessage()));
        }
    }

    public List<Result> results() {
        return List.copyOf(results);
    }

    public record Result(String id, boolean loaded, String message) {
    }
}
