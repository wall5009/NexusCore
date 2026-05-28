package com.rollylindenshnizzer.nexuscore.live;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@NexusStable(since = "1.3")
public final class NexusLiveReload {
    private static final List<ReloadWatchSpec> WATCHERS = new ArrayList<>();

    public static ReloadWatchSpec watch(String name, Path path, ReloadSafety safety) {
        ReloadWatchSpec spec = new ReloadWatchSpec(name, path, safety);
        WATCHERS.add(spec);
        return spec;
    }

    public static List<ReloadWatchSpec> watchers() {
        return List.copyOf(WATCHERS);
    }

    public static ReloadReport runReload(String name, Supplier<ReloadReport> action, boolean production) {
        ReloadWatchSpec spec = WATCHERS.stream().filter(watcher -> watcher.name().equals(name)).findFirst()
                .orElse(new ReloadWatchSpec(name, Path.of("."), ReloadSafety.developmentOnly()));
        if (!spec.safety().allowed(production)) {
            return new ReloadReport(name, false, List.of("Reload blocked by safety policy"), Instant.now());
        }
        return action.get();
    }

    public static String debugSummary() {
        return "reloadWatchers=" + WATCHERS.size();
    }

    @NexusStable(since = "1.3")
    public record ReloadWatchSpec(String name, Path path, ReloadSafety safety) {
    }

    @NexusStable(since = "1.3")
    public record ReloadSafety(boolean allowInProduction, boolean requiresPermission, int maxChangesPerReload) {
        public static ReloadSafety developmentOnly() {
            return new ReloadSafety(false, true, 1_000);
        }

        public static ReloadSafety productionSafe() {
            return new ReloadSafety(true, true, 100);
        }

        public boolean allowed(boolean production) {
            return !production || allowInProduction;
        }
    }

    @NexusStable(since = "1.3")
    public record ReloadReport(String name, boolean success, List<String> messages, Instant completedAt) {
        public ReloadReport {
            messages = List.copyOf(messages);
        }

        public static ReloadReport success(String name, String message) {
            return new ReloadReport(name, true, List.of(message), Instant.now());
        }
    }

    private NexusLiveReload() {
    }
}
