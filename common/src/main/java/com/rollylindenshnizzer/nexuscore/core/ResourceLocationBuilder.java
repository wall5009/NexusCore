package com.rollylindenshnizzer.nexuscore.core;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class ResourceLocationBuilder {
    private final String namespace;
    private final List<String> segments = new ArrayList<>();

    public ResourceLocationBuilder(String namespace) {
        this.namespace = NexusIds.requireNamespace(namespace);
    }

    public ResourceLocationBuilder path(String segment) {
        segments.add(NexusIds.normalizePath(segment));
        return this;
    }

    public ResourceLocationBuilder prefix(String prefix) {
        if (!segments.isEmpty()) {
            segments.set(0, NexusIds.normalizePath(prefix) + "/" + segments.getFirst());
        } else {
            segments.add(NexusIds.normalizePath(prefix));
        }
        return this;
    }

    public ResourceLocationBuilder suffix(String suffix) {
        if (segments.isEmpty()) {
            segments.add(NexusIds.normalizePath(suffix));
        } else {
            int last = segments.size() - 1;
            segments.set(last, segments.get(last) + NexusIds.normalizePath(suffix));
        }
        return this;
    }

    public ResourceLocation build() {
        return NexusIds.id(namespace, String.join("/", segments));
    }
}
