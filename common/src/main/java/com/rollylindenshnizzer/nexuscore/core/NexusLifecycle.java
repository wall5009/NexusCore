package com.rollylindenshnizzer.nexuscore.core;

import dev.architectury.event.events.common.LifecycleEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Loader-neutral lifecycle hooks used by the builders and by downstream mods.
 */
public final class NexusLifecycle {
    private static final Map<Phase, List<Runnable>> LISTENERS = new EnumMap<>(Phase.class);
    private static final EnumSet<Phase> FIRED = EnumSet.noneOf(Phase.class);
    private static boolean hooksInstalled;

    static {
        for (Phase phase : Phase.values()) {
            LISTENERS.put(phase, new ArrayList<>());
        }
    }

    public static void on(Phase phase, Runnable listener) {
        if (FIRED.contains(phase)) {
            listener.run();
            return;
        }
        LISTENERS.get(phase).add(listener);
    }

    public static void fire(Phase phase) {
        if (!FIRED.add(phase)) {
            return;
        }
        for (Runnable listener : List.copyOf(LISTENERS.get(phase))) {
            listener.run();
        }
        LISTENERS.get(phase).clear();
    }

    public static boolean hasFired(Phase phase) {
        return FIRED.contains(phase);
    }

    public static void installArchitecturyHooks() {
        if (hooksInstalled) {
            return;
        }
        hooksInstalled = true;
        LifecycleEvent.SETUP.register(() -> fire(Phase.COMMON_SETUP));
        LifecycleEvent.SERVER_STARTING.register(server -> fire(Phase.SERVER_INIT));
        LifecycleEvent.SERVER_STARTED.register(server -> NexusTasks.runQueued(TaskQueue.SERVER_STARTED));
        LifecycleEvent.SERVER_STOPPING.register(server -> fire(Phase.SERVER_STOPPING));
        LifecycleEvent.SERVER_STOPPED.register(server -> fire(Phase.SERVER_STOPPED));
    }

    private NexusLifecycle() {
    }

    public enum Phase {
        PRE_INIT,
        COMMON_INIT,
        COMMON_SETUP,
        SERVER_INIT,
        CLIENT_INIT,
        DATA_GENERATION_INIT,
        POST_REGISTRY_VALIDATION,
        SERVER_STOPPING,
        SERVER_STOPPED
    }
}
