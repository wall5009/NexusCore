package com.rollylindenshnizzer.nexuscore.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class RecipeJsonBuilder {
    private final JsonObject json = new JsonObject();

    public static RecipeJsonBuilder shaped(String category, String result, int count) {
        RecipeJsonBuilder builder = new RecipeJsonBuilder();
        builder.json.addProperty("type", "minecraft:crafting_shaped");
        builder.json.addProperty("category", category);
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("id", result);
        resultJson.addProperty("count", count);
        builder.json.add("result", resultJson);
        return builder;
    }

    public static RecipeJsonBuilder shapeless(String category, String result, int count) {
        RecipeJsonBuilder builder = new RecipeJsonBuilder();
        builder.json.addProperty("type", "minecraft:crafting_shapeless");
        builder.json.addProperty("category", category);
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("id", result);
        resultJson.addProperty("count", count);
        builder.json.add("result", resultJson);
        builder.json.add("ingredients", new JsonArray());
        return builder;
    }

    public static RecipeJsonBuilder cooking(String type, String category, String ingredient, String result, float experience, int cookingTime) {
        RecipeJsonBuilder builder = new RecipeJsonBuilder();
        builder.json.addProperty("type", type);
        builder.json.addProperty("category", category);
        JsonObject ingredientJson = new JsonObject();
        ingredientJson.addProperty("item", ingredient);
        builder.json.add("ingredient", ingredientJson);
        builder.json.addProperty("result", result);
        builder.json.addProperty("experience", experience);
        builder.json.addProperty("cookingtime", cookingTime);
        return builder;
    }

    public static RecipeJsonBuilder stonecutting(String ingredient, String result, int count) {
        RecipeJsonBuilder builder = new RecipeJsonBuilder();
        builder.json.addProperty("type", "minecraft:stonecutting");
        JsonObject ingredientJson = new JsonObject();
        ingredientJson.addProperty("item", ingredient);
        builder.json.add("ingredient", ingredientJson);
        builder.json.addProperty("result", result);
        builder.json.addProperty("count", count);
        return builder;
    }

    public RecipeJsonBuilder pattern(String... rows) {
        JsonArray pattern = new JsonArray();
        for (String row : rows) {
            pattern.add(row);
        }
        json.add("pattern", pattern);
        return this;
    }

    public RecipeJsonBuilder key(char key, String item) {
        JsonObject keys = json.has("key") ? json.getAsJsonObject("key") : new JsonObject();
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", item);
        keys.add(String.valueOf(key), ingredient);
        json.add("key", keys);
        return this;
    }

    public RecipeJsonBuilder ingredient(String item) {
        JsonArray ingredients = json.getAsJsonArray("ingredients");
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", item);
        ingredients.add(ingredient);
        return this;
    }

    public RecipeJsonBuilder group(String group) {
        json.addProperty("group", group);
        return this;
    }

    public RecipeJsonBuilder criterion(String name, JsonObject criterion) {
        JsonObject criteria = json.has("criteria") ? json.getAsJsonObject("criteria") : new JsonObject();
        criteria.add(name, criterion);
        json.add("criteria", criteria);
        return this;
    }

    public JsonObject build() {
        return json.deepCopy();
    }
}
