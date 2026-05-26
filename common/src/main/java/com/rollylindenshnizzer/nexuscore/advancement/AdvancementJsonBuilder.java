package com.rollylindenshnizzer.nexuscore.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class AdvancementJsonBuilder {
    private final JsonObject root = new JsonObject();
    private final JsonObject criteria = new JsonObject();
    private final JsonArray requirements = new JsonArray();

    public AdvancementJsonBuilder parent(String parent) {
        root.addProperty("parent", parent);
        return this;
    }

    public AdvancementJsonBuilder display(String iconItem, String titleKey, String descriptionKey, String frame, boolean toast, boolean announce, boolean hidden) {
        JsonObject display = new JsonObject();
        JsonObject icon = new JsonObject();
        icon.addProperty("id", iconItem);
        icon.addProperty("count", 1);
        JsonObject title = new JsonObject();
        title.addProperty("translate", titleKey);
        JsonObject description = new JsonObject();
        description.addProperty("translate", descriptionKey);
        display.add("icon", icon);
        display.add("title", title);
        display.add("description", description);
        display.addProperty("frame", frame);
        display.addProperty("show_toast", toast);
        display.addProperty("announce_to_chat", announce);
        display.addProperty("hidden", hidden);
        root.add("display", display);
        return this;
    }

    public AdvancementJsonBuilder background(String texture) {
        root.getAsJsonObject("display").addProperty("background", texture);
        return this;
    }

    public AdvancementJsonBuilder criterion(String name, JsonObject trigger) {
        criteria.add(name, trigger);
        JsonArray requirement = new JsonArray();
        requirement.add(name);
        requirements.add(requirement);
        return this;
    }

    public JsonObject build() {
        root.add("criteria", criteria);
        root.add("requirements", requirements);
        return root.deepCopy();
    }

    public static JsonObject inventoryChanged(String itemId) {
        JsonObject trigger = new JsonObject();
        trigger.addProperty("trigger", "minecraft:inventory_changed");
        JsonObject conditions = new JsonObject();
        JsonArray items = new JsonArray();
        JsonObject item = new JsonObject();
        item.addProperty("items", itemId);
        items.add(item);
        conditions.add("items", items);
        trigger.add("conditions", conditions);
        return trigger;
    }
}
