package com.rollylindenshnizzer.nexuscore.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class LootTableBuilder {
    private final JsonObject root = new JsonObject();
    private final JsonArray pools = new JsonArray();

    private LootTableBuilder(String type) {
        root.addProperty("type", type);
        root.add("pools", pools);
    }

    public static LootTableBuilder block() {
        return new LootTableBuilder("minecraft:block");
    }

    public static LootTableBuilder entity() {
        return new LootTableBuilder("minecraft:entity");
    }

    public LootTableBuilder pool(JsonObject pool) {
        pools.add(pool);
        return this;
    }

    public LootTableBuilder selfDrop(String itemId) {
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", itemId);
        entries.add(entry);
        pool.add("entries", entries);
        pools.add(pool);
        return this;
    }

    public LootTableBuilder explosionSurvives() {
        if (!pools.isEmpty()) {
            JsonObject pool = pools.get(pools.size() - 1).getAsJsonObject();
            JsonArray conditions = pool.has("conditions") ? pool.getAsJsonArray("conditions") : new JsonArray();
            JsonObject condition = new JsonObject();
            condition.addProperty("condition", "minecraft:survives_explosion");
            conditions.add(condition);
            pool.add("conditions", conditions);
        }
        return this;
    }

    public JsonObject build() {
        return root.deepCopy();
    }
}
