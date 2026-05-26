package com.rollylindenshnizzer.nexuscore.math;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public final class NexusMath {
    public static int clamp(int value, int min, int max) {
        return Mth.clamp(value, min, max);
    }

    public static float lerp(float delta, float start, float end) {
        return Mth.lerp(delta, start, end);
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return ((alpha & 255) << 24) | ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
    }

    public static final class WeightedTable<T> {
        private final List<Entry<T>> entries = new ArrayList<>();
        private int totalWeight;

        public WeightedTable<T> add(T value, int weight) {
            if (weight <= 0) {
                return this;
            }
            entries.add(new Entry<>(value, weight));
            totalWeight += weight;
            return this;
        }

        public T pick(RandomSource random) {
            if (entries.isEmpty()) {
                throw new IllegalStateException("Cannot pick from an empty weighted table");
            }
            int target = random.nextInt(totalWeight);
            int cursor = 0;
            for (Entry<T> entry : entries) {
                cursor += entry.weight();
                if (target < cursor) {
                    return entry.value();
                }
            }
            return entries.getLast().value();
        }

        private record Entry<T>(T value, int weight) {
        }
    }

    private NexusMath() {
    }
}
