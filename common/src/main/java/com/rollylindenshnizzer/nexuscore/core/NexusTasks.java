package com.rollylindenshnizzer.nexuscore.core;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;

public final class NexusTasks {
    private static final Map<TaskQueue, Queue<Runnable>> QUEUES = new EnumMap<>(TaskQueue.class);

    static {
        for (TaskQueue queue : TaskQueue.values()) {
            QUEUES.put(queue, new ArrayDeque<>());
        }
    }

    public static void queue(TaskQueue queue, Runnable task) {
        QUEUES.get(queue).add(task);
    }

    public static void afterRegistries(Runnable task) {
        queue(TaskQueue.AFTER_REGISTRIES, task);
    }

    public static void runQueued(TaskQueue queue) {
        Queue<Runnable> tasks = QUEUES.get(queue);
        Runnable task;
        while ((task = tasks.poll()) != null) {
            task.run();
        }
    }

    private NexusTasks() {
    }
}
