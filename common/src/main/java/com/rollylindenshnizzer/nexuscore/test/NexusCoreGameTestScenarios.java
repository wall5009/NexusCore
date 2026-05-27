package com.rollylindenshnizzer.nexuscore.test;

import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerAdvancedControl;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerControlSupport;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerDisplay;
import com.rollylindenshnizzer.nexuscore.config.ConfigSchemaExporter;
import com.rollylindenshnizzer.nexuscore.config.IntOption;
import com.rollylindenshnizzer.nexuscore.config.NexusConfig;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.data.NexusDataValidator;
import com.rollylindenshnizzer.nexuscore.energy.EnergyAccess;
import com.rollylindenshnizzer.nexuscore.energy.EnergyStorage;
import com.rollylindenshnizzer.nexuscore.energy.NexusEnergyStorage;
import com.rollylindenshnizzer.nexuscore.energy.NexusEnergyTransfer;
import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinitions;
import com.rollylindenshnizzer.nexuscore.entity.ProjectileDefinition;
import com.rollylindenshnizzer.nexuscore.fluid.FluidAccess;
import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import com.rollylindenshnizzer.nexuscore.fluid.NexusFluidTank;
import com.rollylindenshnizzer.nexuscore.fluid.NexusFluidTransfer;
import com.rollylindenshnizzer.nexuscore.inventory.InventorySnapshot;
import com.rollylindenshnizzer.nexuscore.inventory.InventoryTransfer;
import com.rollylindenshnizzer.nexuscore.inventory.SimpleItemHandler;
import com.rollylindenshnizzer.nexuscore.inventory.SlotRange;
import com.rollylindenshnizzer.nexuscore.inventory.SlotRole;
import com.rollylindenshnizzer.nexuscore.inventory.TransferRule;
import com.rollylindenshnizzer.nexuscore.machine.MachineProcessResult;
import com.rollylindenshnizzer.nexuscore.machine.MachineProcessingEngine;
import com.rollylindenshnizzer.nexuscore.machine.MachineRecipeDefinition;
import com.rollylindenshnizzer.nexuscore.machine.MachineState;
import com.rollylindenshnizzer.nexuscore.machine.NexusMachineDefinition;
import com.rollylindenshnizzer.nexuscore.machine.NexusMachines;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import com.rollylindenshnizzer.nexuscore.performance.NexusBenchmarks;
import com.rollylindenshnizzer.nexuscore.resource.DataDrivenRegistry;
import com.rollylindenshnizzer.nexuscore.resource.JsonSchema;
import com.rollylindenshnizzer.nexuscore.resource.TypedDataLoader;
import com.rollylindenshnizzer.nexuscore.worldgen.NexusWorldgen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NexusCoreGameTestScenarios {
    public static void energyStorageRateLimits() {
        EnergyStorage energy = new EnergyStorage(1_000, 250, 100);
        assertEquals(250, energy.insert(500, false), "Expected max insert to limit transfer");
        assertEquals(100, energy.extract(500, false), "Expected max extract to limit transfer");
        assertEquals(150, energy.amount(), "Expected remaining energy to be tracked");
    }

    public static void configSchemaExportsSyncedOptions() {
        TestConfig config = new TestConfig();
        JsonObject schema = ConfigSchemaExporter.jsonSchema(config, "gametest");
        String encoded = schema.toString();
        assertTrue(encoded.contains("machine_energy"), "Schema should include the machine energy option");
        assertTrue(encoded.contains("\"serverSynced\":true"), "Schema should mark synced options");
        assertTrue(encoded.contains("\"group\":\"machine\""), "Schema should include option groups");
    }

    public static void datagenValidationCatchesErrorsAndAcceptsValidPlans() {
        NexusData.DataPlan plan = NexusData.plan("nexuscore_gametest")
                .translation("item.nexuscore_gametest.ruby", "Ruby")
                .itemGenerated("ruby")
                .tag("items", "gems/ruby", "nexuscore_gametest:ruby");
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonObject result = new JsonObject();
        result.addProperty("id", "nexuscore_gametest:ruby");
        result.addProperty("count", 1);
        recipe.add("result", result);
        JsonObject criteria = new JsonObject();
        criteria.add("has_ruby", new JsonObject());
        recipe.add("criteria", criteria);
        plan.data("recipe/ruby_copy.json", recipe);
        assertFalse(NexusDataValidator.validatePlan(plan).hasErrors(), "Valid generated data plan should not have errors");

        JsonObject badTag = new JsonObject();
        NexusData.DataPlan invalid = NexusData.plan("nexuscore_bad_gametest").data("tags/items/empty.json", badTag);
        assertTrue(NexusDataValidator.validatePlan(invalid).hasErrors(), "Invalid tag data should produce an error");
    }

    public static void packetDiagnosticsCompareVersions() {
        NexusNetworking.channel("nexuscore", "gametest")
                .protocolVersion("1.1")
                .disconnectOnMismatch((client, server) -> "client=" + client + ", server=" + server);
        var diagnostics = NexusNetworking.diagnostics().get(NexusIds.id("nexuscore", "gametest"));
        assertTrue(diagnostics != null, "Channel diagnostics should be registered");
        var report = diagnostics.compare("1.0", Set.of());
        assertFalse(report.compatible(), "Mismatched protocol versions should be incompatible");
        assertTrue(report.message().contains("client=1.0"), "Mismatch report should use the configured message");
    }

    public static void recipeViewerControlsHavePortableFallbacks() {
        RecipeViewerDisplay display = RecipeViewerDisplay.builder(
                        NexusIds.id("nexuscore", "gametest_recipe"),
                        NexusIds.id("nexuscore", "gametest_category"), 120, 60)
                .page(page -> page
                        .itemInput(4, 20, new ItemStack(Items.STONE))
                        .arrowProgress(40, 20, 200)
                        .itemOutput(78, 20, new ItemStack(Items.DIAMOND))
                        .control(RecipeViewerAdvancedControl.button("all", 4, 44, 36, 16, "Info"))
                        .control(RecipeViewerAdvancedControl.recipeTransferButton(80, 44)))
                .build();
        for (RecipeViewerAdvancedControl control : display.layout().controlsFor(display.layout().pages().getFirst())) {
            assertTrue(RecipeViewerControlSupport.supports("jei", control)
                            || !RecipeViewerControlSupport.fallbackTooltip("jei", control).isBlank(),
                    "JEI controls should either be native or have a deterministic fallback");
            assertTrue(RecipeViewerControlSupport.supports("emi", control)
                            || !RecipeViewerControlSupport.fallbackTooltip("emi", control).isBlank(),
                    "EMI controls should either be native or have a deterministic fallback");
            assertTrue(RecipeViewerControlSupport.supports("rei", control)
                            || !RecipeViewerControlSupport.fallbackTooltip("rei", control).isBlank(),
                    "REI controls should either be native or have a deterministic fallback");
        }
    }

    public static void benchmarkSuiteRecordsProfilerSamples() {
        var results = NexusBenchmarks.smokeSuite().run();
        assertTrue(results.size() >= 2, "Smoke benchmark suite should run multiple benchmarks");
        assertTrue(results.stream().allMatch(result -> result.iterations() > 0), "Benchmarks should report iterations");
    }

    public static void machineFrameworkProcessesHybridRecipes() {
        NexusMachineDefinition definition = NexusMachines.register(NexusMachines.machine("nexuscore", "gametest_press")
                .category("press")
                .energy(10_000, 250, 250)
                .fluid(4_000)
                .slots("input", SlotRole.INPUT, 0, 1)
                .slots("output", SlotRole.OUTPUT, 1, 2)
                .build());
        SimpleItemHandler inventory = new SimpleItemHandler(2);
        inventory.set(0, new ItemStack(Items.RAW_IRON, 1));
        NexusEnergyStorage energy = new NexusEnergyStorage(10_000, 250, 250);
        energy.insert(1_000, false);
        NexusFluidTank fluid = new NexusFluidTank(4_000);
        fluid.fill(new FluidStack(Fluids.WATER, 1_000), false);
        MachineRecipeDefinition recipe = MachineRecipeDefinition.builder(
                        NexusIds.id("nexuscore", "gametest_press_recipe"),
                        NexusIds.id("nexuscore", "pressing"))
                .input(new ItemStack(Items.RAW_IRON))
                .fluidInput(new FluidStack(Fluids.WATER, 250))
                .output(new ItemStack(Items.IRON_INGOT))
                .energy(100)
                .ticks(2)
                .build();
        MachineProcessingEngine engine = new MachineProcessingEngine(definition, inventory, energy, fluid, new MachineState(), new java.util.Random(1L));

        MachineProcessResult first = engine.tick(List.of(recipe), false);
        MachineProcessResult second = engine.tick(List.of(recipe), false);

        assertTrue(first.progressed(), "Machine should progress on first tick");
        assertTrue(second.completed(), "Machine should complete recipe on second tick");
        assertTrue(inventory.get(0).isEmpty(), "Input should be consumed");
        assertEquals(1, inventory.get(1).getCount(), "Output should be inserted");
        assertEquals(750, fluid.stored().amount(), "Fluid cost should be drained");
    }

    public static void inventoryTransferRulesTraceRoutes() {
        SimpleItemHandler inventory = new SimpleItemHandler(3);
        inventory.set(0, new ItemStack(Items.COPPER_INGOT, 8));
        TransferRule rule = TransferRule.builder("input-to-output", new SlotRange(0, 1), new SlotRange(1, 3))
                .player(true)
                .automation(false)
                .build();
        InventorySnapshot before = InventorySnapshot.capture(inventory);
        var result = InventoryTransfer.route(inventory, 0, TransferRule.TransferActor.PLAYER, List.of(rule), false);
        InventorySnapshot after = InventorySnapshot.capture(inventory);

        assertEquals(8, result.moved(), "Transfer should move the whole stack");
        assertTrue(after.diff(before).size() >= 2, "Inventory diff should report changed source and target slots");
        assertTrue(result.trace().stream().anyMatch(line -> line.contains("input-to-output")), "Transfer trace should name the route");
    }

    public static void energyAndFluidTransfersRespectSides() {
        NexusEnergyStorage source = NexusEnergyStorage.builder(1_000).io(1_000, 1_000)
                .side(Direction.EAST, EnergyAccess.OUTPUT)
                .build();
        NexusEnergyStorage target = NexusEnergyStorage.builder(1_000).io(1_000, 1_000)
                .side(Direction.WEST, EnergyAccess.INPUT)
                .build();
        source.insert(500, false);
        var energyResult = NexusEnergyTransfer.move(source, Direction.EAST, target, Direction.WEST, 200, false);
        assertEquals(200, energyResult.moved(), "Side-aware energy transfer should move allowed energy");

        NexusFluidTank fluidSource = NexusFluidTank.builder(1_000).side(Direction.EAST, FluidAccess.OUTPUT).build();
        NexusFluidTank fluidTarget = NexusFluidTank.builder(1_000).side(Direction.WEST, FluidAccess.INPUT).build();
        fluidSource.fill(new FluidStack(Fluids.WATER, 500), false);
        var fluidResult = NexusFluidTransfer.move(fluidSource, Direction.EAST, fluidTarget, Direction.WEST, 250, false);
        assertEquals(250, fluidResult.moved().amount(), "Side-aware fluid transfer should move allowed fluid");
    }

    public static void worldgenAndEntityDefinitionsGenerateDescriptors() {
        var ore = NexusWorldgen.ore("nexuscore", "gametest_ore")
                .state("minecraft:diamond_ore")
                .veinSize(5)
                .count(3)
                .heightRange(-16, 32);
        assertTrue(ore.configuredFeature().toString().contains("minecraft:ore"), "Ore builder should emit a configured feature");
        assertTrue(ore.placedFeature().toString().contains("height_range"), "Ore builder should emit placement modifiers");

        var projectile = NexusEntityDefinitions.register(NexusEntityDefinitions.projectile("nexuscore", "gametest_bolt")
                .sized(0.25F, 0.25F)
                .projectile(ProjectileDefinition.simple(4.0, 1.6F))
                .spawnEgg(0x3355FF, 0xFFFFFF)
                .build());
        assertTrue(projectile.projectile() != null, "Projectile definition should keep projectile settings");
        assertTrue(NexusEntityDefinitions.definitions().contains(projectile), "Entity definition registry should retain the descriptor");
    }

    public static void typedDataLoaderValidatesDatapackJson() {
        TypedDataLoader<String> loader = new TypedDataLoader<>("machines",
                new JsonSchema().require("type", JsonSchema.Type.STRING).require("energy", JsonSchema.Type.NUMBER),
                json -> json.get("type").getAsString() + ":" + json.get("energy").getAsInt());
        DataDrivenRegistry<String> registry = new DataDrivenRegistry<>(loader);
        JsonObject valid = new JsonObject();
        valid.addProperty("type", "nexuscore:pressing");
        valid.addProperty("energy", 100);
        JsonObject invalid = new JsonObject();
        invalid.addProperty("type", "nexuscore:pressing");

        var report = registry.reload(Map.of(
                ResourceLocation.fromNamespaceAndPath("nexuscore", "valid"), valid,
                ResourceLocation.fromNamespaceAndPath("nexuscore", "invalid"), invalid));

        assertFalse(report.passed(), "Invalid datapack JSON should be reported");
        assertTrue(registry.get(ResourceLocation.fromNamespaceAndPath("nexuscore", "valid")).isPresent(), "Valid datapack JSON should load");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static final class TestConfig extends NexusConfig {
        private final IntOption machineEnergy;

        private TestConfig() {
            super("nexuscore_gametest_config");
            machineEnergy = intOption("machine_energy", 100).range(1, 1_000);
            machineEnergy.group("machine").serverSynced().comment("Energy per operation");
        }
    }

    private NexusCoreGameTestScenarios() {
    }
}
