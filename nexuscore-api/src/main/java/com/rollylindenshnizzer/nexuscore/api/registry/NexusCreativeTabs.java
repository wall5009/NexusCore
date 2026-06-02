package com.rollylindenshnizzer.nexuscore.api.registry;

import com.rollylindenshnizzer.nexuscore.api.NexusMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NexusCreativeTabs {
    private NexusCreativeTabs() {
    }

    public static Builder create(NexusMod mod, String id) {
        return new Builder(mod.id(), id);
    }

    public static final class Builder {
        private final String modId;
        private final String id;
        private String title;
        private String iconItem;
        private final List<String> entries = new ArrayList<>();

        private Builder(String modId, String id) {
            this.modId = Objects.requireNonNull(modId, "modId");
            this.id = Objects.requireNonNull(id, "id");
            this.title = id;
        }

        public Builder title(String title) {
            this.title = Objects.requireNonNull(title, "title");
            return this;
        }

        public Builder icon(String itemId) {
            this.iconItem = Objects.requireNonNull(itemId, "itemId");
            return this;
        }

        public Builder entry(String entryId) {
            this.entries.add(Objects.requireNonNull(entryId, "entryId"));
            return this;
        }

        public NexusEntry<NexusCreativeTabDefinition> register() {
            NexusCreativeTabDefinition definition = new NexusCreativeTabDefinition(modId, id, title, iconItem, entries);
            return NexusRegistries.register(NexusRegistries.CREATIVE_TABS, definition.fullId(), ignored -> definition);
        }
    }
}
