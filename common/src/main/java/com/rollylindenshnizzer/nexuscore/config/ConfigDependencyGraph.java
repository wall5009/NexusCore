package com.rollylindenshnizzer.nexuscore.config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ConfigDependencyGraph {
    public static Report analyze(NexusConfig config) {
        List<String> issues = new ArrayList<>();
        for (ConfigOption<?> option : config.options().values()) {
            for (String dependency : option.dependencies()) {
                if (!config.options().containsKey(dependency)) {
                    issues.add(option.key() + " depends on missing option " + dependency);
                }
            }
            for (String conflict : option.conflicts()) {
                if (!config.options().containsKey(conflict)) {
                    issues.add(option.key() + " conflicts with missing option " + conflict);
                }
            }
        }
        for (String key : config.options().keySet()) {
            detectCycles(config, key, new ArrayDeque<>(), new HashSet<>(), issues);
        }
        return new Report(issues);
    }

    private static void detectCycles(NexusConfig config, String key, ArrayDeque<String> stack, Set<String> visited,
                                     List<String> issues) {
        if (stack.contains(key)) {
            issues.add("Circular config dependency: " + String.join(" -> ", stack) + " -> " + key);
            return;
        }
        if (!visited.add(key)) {
            return;
        }
        ConfigOption<?> option = config.options().get(key);
        if (option == null) {
            return;
        }
        stack.addLast(key);
        for (String dependency : option.dependencies()) {
            detectCycles(config, dependency, stack, visited, issues);
        }
        stack.removeLast();
    }

    public record Report(List<String> issues) {
        public boolean valid() {
            return issues.isEmpty();
        }

        public String debugOutput() {
            return valid() ? "Config dependency graph is valid" : String.join("\n", issues);
        }
    }

    private ConfigDependencyGraph() {
    }
}
