package com.rollylindenshnizzer.nexuscore.performance;

import net.minecraft.world.level.ChunkPos;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public final class ChunkTaskQueue {
    private final Map<ChunkPos, Queue<Runnable>> tasks = new HashMap<>();

    public void queue(ChunkPos pos, Runnable task) {
        tasks.computeIfAbsent(pos, ignored -> new ArrayDeque<>()).add(task);
    }

    public int run(ChunkPos pos, int maxTasks) {
        Queue<Runnable> queue = tasks.get(pos);
        if (queue == null) {
            return 0;
        }
        int ran = 0;
        while (ran < maxTasks && !queue.isEmpty()) {
            queue.remove().run();
            ran++;
        }
        if (queue.isEmpty()) {
            tasks.remove(pos);
        }
        return ran;
    }
}
