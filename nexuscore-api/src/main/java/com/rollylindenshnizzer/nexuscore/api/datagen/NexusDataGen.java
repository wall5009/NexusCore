package com.rollylindenshnizzer.nexuscore.api.datagen;

import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NexusDataGen {
    private final String modId;
    private final List<NexusGeneratedResource> resources;

    private NexusDataGen(String modId, List<NexusGeneratedResource> resources) {
        this.modId = modId;
        this.resources = List.copyOf(resources);
    }

    public static Builder create(String modId) {
        return new Builder(modId);
    }

    public String modId() {
        return modId;
    }

    public List<NexusGeneratedResource> resources() {
        return new ArrayList<>(resources);
    }

    public void run() {
        NexusServices.get().datagen().generate(this);
    }

    public static final class Builder {
        private final String modId;
        private final List<NexusGeneratedResource> resources = new ArrayList<>();

        private Builder(String modId) {
            this.modId = Objects.requireNonNull(modId, "modId");
        }

        public Builder recipe(String id, String json) {
            return resource("data/" + modId + "/recipes/" + id + ".json", json);
        }

        public Builder lootTable(String id, String json) {
            return resource("data/" + modId + "/loot_tables/" + id + ".json", json);
        }

        public Builder tag(String registry, String id, String json) {
            return resource("data/" + modId + "/tags/" + registry + "/" + id + ".json", json);
        }

        public Builder blockstate(String id, String json) {
            return resource("assets/" + modId + "/blockstates/" + id + ".json", json);
        }

        public Builder model(String path, String json) {
            return resource("assets/" + modId + "/models/" + path + ".json", json);
        }

        public Builder lang(String locale, String json) {
            return resource("assets/" + modId + "/lang/" + locale + ".json", json);
        }

        public Builder resource(String path, String json) {
            resources.add(new NexusGeneratedResource(path, json));
            return this;
        }

        public NexusDataGen build() {
            return new NexusDataGen(modId, resources);
        }
    }
}
