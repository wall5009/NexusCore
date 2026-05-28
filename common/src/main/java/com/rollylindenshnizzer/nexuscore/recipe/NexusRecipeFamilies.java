package com.rollylindenshnizzer.nexuscore.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NexusStable(since = "1.3")
public final class NexusRecipeFamilies {
    private static final Map<ResourceLocation, RecipeFamily> FAMILIES = new LinkedHashMap<>();

    public static RecipeFamily.Builder family(String namespace, String path) {
        return new RecipeFamily.Builder(NexusIds.id(namespace, path));
    }

    public static RecipeFamily register(RecipeFamily family) {
        FAMILIES.put(family.id(), family);
        return family;
    }

    public static Collection<RecipeFamily> families() {
        return List.copyOf(FAMILIES.values());
    }

    public static RecipeBalanceReport balanceReport() {
        List<String> warnings = new ArrayList<>();
        for (RecipeFamily family : FAMILIES.values()) {
            for (AdvancedMachineRecipe recipe : family.recipes()) {
                if (recipe.energy() <= 0 && recipe.ticks() <= 1) {
                    warnings.add(recipe.id() + " is effectively free and instant");
                }
                if (recipe.chanceOutputs().stream().anyMatch(output -> output.chance() <= 0.0D || output.chance() > 1.0D)) {
                    warnings.add(recipe.id() + " has chance output outside 0..1");
                }
            }
        }
        return new RecipeBalanceReport(FAMILIES.size(), warnings);
    }

    @NexusStable(since = "1.3")
    public record RecipeFamily(ResourceLocation id,
                               String category,
                               List<AdvancedMachineRecipe> recipes,
                               RecipeGuideIndex guideIndex) {
        public RecipeFamily {
            recipes = List.copyOf(recipes);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id.toString());
            json.addProperty("category", category);
            JsonArray array = new JsonArray();
            for (AdvancedMachineRecipe recipe : recipes) {
                array.add(recipe.toJson());
            }
            json.add("recipes", array);
            json.add("guide", guideIndex.toJson());
            return json;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private String category = "processing";
            private final List<AdvancedMachineRecipe> recipes = new ArrayList<>();
            private RecipeGuideIndex guideIndex = new RecipeGuideIndex(List.of(), List.of());

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder category(String category) {
                this.category = category;
                return this;
            }

            public Builder recipe(AdvancedMachineRecipe recipe) {
                this.recipes.add(recipe);
                return this;
            }

            public Builder guide(RecipeGuideIndex guideIndex) {
                this.guideIndex = guideIndex;
                return this;
            }

            public RecipeFamily build() {
                return new RecipeFamily(id, category, recipes, guideIndex);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record AdvancedMachineRecipe(ResourceLocation id,
                                        List<String> itemInputs,
                                        List<String> fluidInputs,
                                        List<String> itemOutputs,
                                        List<String> fluidOutputs,
                                        List<ChanceOutput> chanceOutputs,
                                        int energy,
                                        int ticks,
                                        Map<String, String> conditions) {
        public AdvancedMachineRecipe {
            itemInputs = List.copyOf(itemInputs);
            fluidInputs = List.copyOf(fluidInputs);
            itemOutputs = List.copyOf(itemOutputs);
            fluidOutputs = List.copyOf(fluidOutputs);
            chanceOutputs = List.copyOf(chanceOutputs);
            conditions = Map.copyOf(conditions);
        }

        public static Builder builder(String namespace, String path) {
            return new Builder(NexusIds.id(namespace, path));
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id.toString());
            json.add("item_inputs", strings(itemInputs));
            json.add("fluid_inputs", strings(fluidInputs));
            json.add("item_outputs", strings(itemOutputs));
            json.add("fluid_outputs", strings(fluidOutputs));
            JsonArray chance = new JsonArray();
            chanceOutputs.forEach(output -> chance.add(output.toJson()));
            json.add("chance_outputs", chance);
            json.addProperty("energy", energy);
            json.addProperty("ticks", ticks);
            JsonObject conditionJson = new JsonObject();
            conditions.forEach(conditionJson::addProperty);
            json.add("conditions", conditionJson);
            return json;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private final List<String> itemInputs = new ArrayList<>();
            private final List<String> fluidInputs = new ArrayList<>();
            private final List<String> itemOutputs = new ArrayList<>();
            private final List<String> fluidOutputs = new ArrayList<>();
            private final List<ChanceOutput> chanceOutputs = new ArrayList<>();
            private final Map<String, String> conditions = new LinkedHashMap<>();
            private int energy = 100;
            private int ticks = 100;

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder itemInput(String item) {
                this.itemInputs.add(item);
                return this;
            }

            public Builder fluidInput(String fluid) {
                this.fluidInputs.add(fluid);
                return this;
            }

            public Builder itemOutput(String item) {
                this.itemOutputs.add(item);
                return this;
            }

            public Builder fluidOutput(String fluid) {
                this.fluidOutputs.add(fluid);
                return this;
            }

            public Builder chanceOutput(String item, int count, double chance) {
                this.chanceOutputs.add(new ChanceOutput(item, count, chance));
                return this;
            }

            public Builder energy(int energy) {
                this.energy = energy;
                return this;
            }

            public Builder ticks(int ticks) {
                this.ticks = ticks;
                return this;
            }

            public Builder condition(String key, String value) {
                this.conditions.put(key, value);
                return this;
            }

            public AdvancedMachineRecipe build() {
                return new AdvancedMachineRecipe(id, itemInputs, fluidInputs, itemOutputs, fluidOutputs,
                        chanceOutputs, energy, ticks, conditions);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record ChanceOutput(String item, int count, double chance) {
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("item", item);
            json.addProperty("count", count);
            json.addProperty("chance", chance);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record RecipeGuideIndex(List<String> chapters, List<String> searchKeywords) {
        public RecipeGuideIndex {
            chapters = List.copyOf(chapters);
            searchKeywords = List.copyOf(searchKeywords);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.add("chapters", strings(chapters));
            json.add("search_keywords", strings(searchKeywords));
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record RecipeTransferPlan(ResourceLocation recipeId,
                                     TransferTarget target,
                                     List<String> ghostIngredients,
                                     List<String> missingItems,
                                     List<String> fluidRequirements,
                                     int energyRequirement,
                                     List<String> catalysts,
                                     List<String> failureReasons) {
        public RecipeTransferPlan {
            ghostIngredients = List.copyOf(ghostIngredients);
            missingItems = List.copyOf(missingItems);
            fluidRequirements = List.copyOf(fluidRequirements);
            catalysts = List.copyOf(catalysts);
            failureReasons = List.copyOf(failureReasons);
        }

        public boolean transferable() {
            return failureReasons.isEmpty();
        }
    }

    @NexusStable(since = "1.3")
    public enum TransferTarget {
        MACHINE,
        MULTIBLOCK,
        RITUAL,
        CUSTOM_CRAFTING
    }

    @NexusStable(since = "1.3")
    public record CustomCraftingSystem(ResourceLocation id,
                                       CraftingKind kind,
                                       List<String> inputs,
                                       List<String> environmentRequirements,
                                       String assistantEntity,
                                       String dimension,
                                       String structureRequirement) {
        public CustomCraftingSystem {
            inputs = List.copyOf(inputs);
            environmentRequirements = List.copyOf(environmentRequirements);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id.toString());
            json.addProperty("kind", kind.name().toLowerCase(java.util.Locale.ROOT));
            json.add("inputs", strings(inputs));
            json.add("environment_requirements", strings(environmentRequirements));
            json.addProperty("assistant_entity", assistantEntity);
            json.addProperty("dimension", dimension);
            json.addProperty("structure_requirement", structureRequirement);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public enum CraftingKind {
        NON_GRID,
        ALTAR,
        TIMED,
        ENVIRONMENT,
        ENTITY_ASSISTED,
        DIMENSION_SPECIFIC,
        MULTIBLOCK,
        RITUAL
    }

    @NexusStable(since = "1.3")
    public record ProcessingChainReport(List<ResourceLocation> recipes,
                                        List<String> missingRecipes,
                                        List<String> circularDependencies,
                                        Map<String, Double> inputOutputRatios,
                                        Map<String, Integer> machineTiers,
                                        Map<String, Integer> energyCosts,
                                        Map<String, Integer> fluidCosts) {
        public ProcessingChainReport {
            recipes = List.copyOf(recipes);
            missingRecipes = List.copyOf(missingRecipes);
            circularDependencies = List.copyOf(circularDependencies);
            inputOutputRatios = Map.copyOf(inputOutputRatios);
            machineTiers = Map.copyOf(machineTiers);
            energyCosts = Map.copyOf(energyCosts);
            fluidCosts = Map.copyOf(fluidCosts);
        }

        public boolean healthy() {
            return missingRecipes.isEmpty() && circularDependencies.isEmpty();
        }
    }

    public static ProcessingChainReport processingChain(Collection<AdvancedMachineRecipe> recipes) {
        List<ResourceLocation> ids = recipes.stream().map(AdvancedMachineRecipe::id).toList();
        Map<String, Double> ratios = new LinkedHashMap<>();
        Map<String, Integer> energy = new LinkedHashMap<>();
        Map<String, Integer> fluids = new LinkedHashMap<>();
        for (AdvancedMachineRecipe recipe : recipes) {
            ratios.put(recipe.id().toString(), (double) Math.max(1, recipe.itemOutputs().size()) / Math.max(1, recipe.itemInputs().size()));
            energy.put(recipe.id().toString(), recipe.energy());
            fluids.put(recipe.id().toString(), recipe.fluidInputs().size());
        }
        return new ProcessingChainReport(ids, List.of(), List.of(), ratios, Map.of(), energy, fluids);
    }

    @NexusStable(since = "1.3")
    public record RecipeBalanceReport(int familyCount, List<String> warnings) {
        public RecipeBalanceReport {
            warnings = List.copyOf(warnings);
        }
    }

    private static JsonArray strings(Collection<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private NexusRecipeFamilies() {
    }
}
