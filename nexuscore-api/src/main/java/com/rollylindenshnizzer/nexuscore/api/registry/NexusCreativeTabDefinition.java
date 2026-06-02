package com.rollylindenshnizzer.nexuscore.api.registry;

import java.util.ArrayList;
import java.util.List;

public final class NexusCreativeTabDefinition {
    private final String modId;
    private final String id;
    private final String title;
    private final String iconItem;
    private final List<String> entries;

    public NexusCreativeTabDefinition(String modId, String id, String title, String iconItem, List<String> entries) {
        this.modId = modId;
        this.id = id;
        this.title = title;
        this.iconItem = iconItem;
        this.entries = List.copyOf(entries);
    }

    public String modId() {
        return modId;
    }

    public String id() {
        return id;
    }

    public String fullId() {
        return modId + ":" + id;
    }

    public String title() {
        return title;
    }

    public String iconItem() {
        return iconItem;
    }

    public List<String> entries() {
        return new ArrayList<>(entries);
    }
}
