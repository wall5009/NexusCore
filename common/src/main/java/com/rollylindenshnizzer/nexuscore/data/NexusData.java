package com.rollylindenshnizzer.nexuscore.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.core.NexusException;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.registry.NexusContentManifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NexusData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, DataPlan> PLANS = new ConcurrentHashMap<>();

    public static DataPlan plan(String modId) {
        return PLANS.computeIfAbsent(NexusIds.requireNamespace(modId), DataPlan::new);
    }

    public static Map<String, DataPlan> plans() {
        return Map.copyOf(PLANS);
    }

    private NexusData() {
    }

    public static final class DataPlan {
        private final String modId;
        private final Map<String, String> translations = new LinkedHashMap<>();
        private final Map<String, JsonObject> assets = new LinkedHashMap<>();
        private final Map<String, JsonObject> data = new LinkedHashMap<>();

        private DataPlan(String modId) {
            this.modId = modId;
        }

        public DataPlan translation(String key, String value) {
            translations.put(key, value);
            NexusContentManifest.recordGenerated(modId, "translation", key);
            return this;
        }

        public DataPlan itemGenerated(String path) {
            JsonObject model = new JsonObject();
            model.addProperty("parent", "minecraft:item/generated");
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", modId + ":item/" + path);
            model.add("textures", textures);
            assets.put("models/item/" + path + ".json", model);
            NexusContentManifest.recordGenerated(modId, "item_model", "models/item/" + path + ".json");
            return this;
        }

        public DataPlan blockCubeAll(String path) {
            JsonObject model = new JsonObject();
            model.addProperty("parent", "minecraft:block/cube_all");
            JsonObject textures = new JsonObject();
            textures.addProperty("all", modId + ":block/" + path);
            model.add("textures", textures);
            assets.put("models/block/" + path + ".json", model);
            NexusContentManifest.recordGenerated(modId, "block_model", "models/block/" + path + ".json");

            JsonObject itemModel = new JsonObject();
            itemModel.addProperty("parent", modId + ":block/" + path);
            assets.put("models/item/" + path + ".json", itemModel);
            NexusContentManifest.recordGenerated(modId, "item_model", "models/item/" + path + ".json");

            JsonObject blockstate = new JsonObject();
            JsonObject variants = new JsonObject();
            JsonObject normal = new JsonObject();
            normal.addProperty("model", modId + ":block/" + path);
            variants.add("", normal);
            blockstate.add("variants", variants);
            assets.put("blockstates/" + path + ".json", blockstate);
            NexusContentManifest.recordGenerated(modId, "blockstate", "blockstates/" + path + ".json");
            return this;
        }

        public DataPlan lootDropsSelf(String path) {
            JsonObject loot = new JsonObject();
            loot.addProperty("type", "minecraft:block");
            JsonArray pools = new JsonArray();
            JsonObject pool = new JsonObject();
            pool.addProperty("rolls", 1);
            JsonArray entries = new JsonArray();
            JsonObject entry = new JsonObject();
            entry.addProperty("type", "minecraft:item");
            entry.addProperty("name", modId + ":" + path);
            entries.add(entry);
            pool.add("entries", entries);
            pools.add(pool);
            loot.add("pools", pools);
            data.put("loot_table/blocks/" + path + ".json", loot);
            NexusContentManifest.recordGenerated(modId, "loot_table", "loot_table/blocks/" + path + ".json");
            return this;
        }

        public DataPlan tag(String registryFolder, String tagPath, String... values) {
            JsonObject tag = new JsonObject();
            tag.addProperty("replace", false);
            JsonArray array = new JsonArray();
            for (String value : values) {
                array.add(value);
            }
            tag.add("values", array);
            data.put("tags/" + registryFolder + "/" + tagPath + ".json", tag);
            NexusContentManifest.recordGenerated(modId, "tag", "tags/" + registryFolder + "/" + tagPath + ".json");
            return this;
        }

        public DataPlan asset(String relativePath, JsonObject json) {
            assets.put(relativePath, json.deepCopy());
            NexusContentManifest.recordGenerated(modId, "asset", relativePath);
            return this;
        }

        public DataPlan data(String relativePath, JsonObject json) {
            data.put(relativePath, json.deepCopy());
            NexusContentManifest.recordGenerated(modId, "data", relativePath);
            return this;
        }

        public Map<String, String> translations() {
            return Map.copyOf(translations);
        }

        public String modId() {
            return modId;
        }

        public Map<String, JsonObject> assets() {
            return Map.copyOf(assets);
        }

        public Map<String, JsonObject> data() {
            return Map.copyOf(data);
        }

        public void writeTo(Path generatedRoot) {
            try {
                if (!translations.isEmpty()) {
                    Path lang = generatedRoot.resolve("assets").resolve(modId).resolve("lang").resolve("en_us.json");
                    Files.createDirectories(lang.getParent());
                    Files.writeString(lang, GSON.toJson(translations));
                }
                for (Map.Entry<String, JsonObject> entry : assets.entrySet()) {
                    writeJson(generatedRoot.resolve("assets").resolve(modId).resolve(entry.getKey()), entry.getValue());
                }
                for (Map.Entry<String, JsonObject> entry : data.entrySet()) {
                    writeJson(generatedRoot.resolve("data").resolve(modId).resolve(entry.getKey()), entry.getValue());
                }
                writeJson(generatedRoot.resolve("data").resolve(modId).resolve("nexus.content.json"),
                        NexusContentManifest.json(modId));
            } catch (IOException exception) {
                throw new NexusException("Failed to write generated data for " + modId, exception);
            }
        }

        private static void writeJson(Path path, JsonObject json) throws IOException {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(json));
        }
    }
}
