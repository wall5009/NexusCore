package com.rollylindenshnizzer.nexuscore.resource;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.util.List;

@NexusStable(since = "1.2")
public record ResourceValidationReport(List<String> loaded,
                                       List<String> errors,
                                       List<String> warnings) {
    public ResourceValidationReport {
        loaded = loaded == null ? List.of() : List.copyOf(loaded);
        errors = errors == null ? List.of() : List.copyOf(errors);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public boolean passed() {
        return errors.isEmpty();
    }

    public String summary() {
        return "loaded=" + loaded.size() + ", errors=" + errors.size() + ", warnings=" + warnings.size();
    }
}
