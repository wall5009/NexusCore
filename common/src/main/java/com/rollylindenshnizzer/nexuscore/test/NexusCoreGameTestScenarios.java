package com.rollylindenshnizzer.nexuscore.test;

import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.ai.NexusAi;
import com.rollylindenshnizzer.nexuscore.automation.NexusAutomation;
import com.rollylindenshnizzer.nexuscore.authoring.NexusAuthoring;
import com.rollylindenshnizzer.nexuscore.balance.NexusBalance;
import com.rollylindenshnizzer.nexuscore.biome.NexusBiomes;
import com.rollylindenshnizzer.nexuscore.compat.NexusCompatibility;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerAdvancedControl;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerControlSupport;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerDisplay;
import com.rollylindenshnizzer.nexuscore.config.ConfigSchemaExporter;
import com.rollylindenshnizzer.nexuscore.config.IntOption;
import com.rollylindenshnizzer.nexuscore.config.NexusConfig;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.data.NexusDataValidator;
import com.rollylindenshnizzer.nexuscore.dimension.NexusDimensions;
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
import com.rollylindenshnizzer.nexuscore.live.NexusLiveReload;
import com.rollylindenshnizzer.nexuscore.machine.MachineProcessResult;
import com.rollylindenshnizzer.nexuscore.machine.MachineProcessingEngine;
import com.rollylindenshnizzer.nexuscore.machine.MachineRecipeDefinition;
import com.rollylindenshnizzer.nexuscore.machine.MachineState;
import com.rollylindenshnizzer.nexuscore.machine.NexusMachineDefinition;
import com.rollylindenshnizzer.nexuscore.machine.NexusMachines;
import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblockPredicates;
import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblocks;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import com.rollylindenshnizzer.nexuscore.performance.NexusBenchmarks;
import com.rollylindenshnizzer.nexuscore.progression.NexusProgression;
import com.rollylindenshnizzer.nexuscore.recipe.NexusRecipeFamilies;
import com.rollylindenshnizzer.nexuscore.resource.DataDrivenRegistry;
import com.rollylindenshnizzer.nexuscore.resource.JsonSchema;
import com.rollylindenshnizzer.nexuscore.resource.NexusDataDefinitions;
import com.rollylindenshnizzer.nexuscore.ritual.NexusRitualActions;
import com.rollylindenshnizzer.nexuscore.ritual.NexusRituals;
import com.rollylindenshnizzer.nexuscore.ritual.NexusTime;
import com.rollylindenshnizzer.nexuscore.ritual.NexusWeather;
import com.rollylindenshnizzer.nexuscore.resource.TypedDataLoader;
import com.rollylindenshnizzer.nexuscore.runtime.NexusGeneratedPackResources;
import com.rollylindenshnizzer.nexuscore.runtime.NexusRuntimeContent;
import com.rollylindenshnizzer.nexuscore.runtime.NexusRuntimeRepositorySource;
import com.rollylindenshnizzer.nexuscore.security.NexusSafety;
import com.rollylindenshnizzer.nexuscore.simulation.NexusSimulation;
import com.rollylindenshnizzer.nexuscore.structure.NexusStructures;
import com.rollylindenshnizzer.nexuscore.worldgen.NexusWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.material.Fluids;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public static void v13DimensionAndPortalDefinitionsValidate() {
        var dimension = NexusDimensions.register(NexusDimensions.dimension("nexuscore", "gametest_dimension")
                .singleBiome("minecraft:plains")
                .config(new NexusDimensions.DimensionConfigProfile(true, false, 1.0D, 4_000, 6_000))
                .spawnRules(new NexusDimensions.DimensionSpawnRules(true, true, 2, List.of("minecraft:cow")))
                .build());
        var portal = NexusDimensions.registerPortal(NexusDimensions.portal("nexuscore", "gametest_portal")
                .targetDimension(dimension.id())
                .frame("minecraft:obsidian", 4, 5)
                .build());

        assertTrue(portal.frame().matches(List.of("####", "#  #", "#  #", "#  #", "####")), "Portal frame matcher should accept valid hollow frames");
        assertTrue(NexusDimensions.validate().passed(), "Registered dimension and portal should validate");
        assertTrue(dimension.toDimensionJson().toString().contains("single:minecraft:plains"), "Dimension datagen should include biome source");
    }

    public static void v13StructureRulesExplainPlacement() {
        var metadata = NexusStructures.StructureTemplateMetadata.single("nexuscore:structures/gametest_tower.nbt")
                .size(5, 9, 5)
                .lootTable("nexuscore:chests/gametest_tower")
                .requiredBlock("minecraft:stone_bricks");
        var placement = NexusStructures.StructurePlacementRule.common().spacing(24, 8).height(64, 180).rarity(0.75D);
        var structure = NexusStructures.register(NexusStructures.structure("nexuscore", "gametest_tower")
                .template(metadata)
                .placement(placement)
                .biome("#minecraft:is_overworld")
                .dimension("minecraft:overworld")
                .build());

        assertTrue(structure.explainPlacement().contains("spacing=24"), "Structure placement explanation should include spacing");
        assertTrue(NexusStructures.validate().passed(), "Structure definition should validate");
    }

    public static void v13BiomeAndAiHelpersProduceDebugReports() {
        NexusBiomes.register(NexusBiomes.biome("nexuscore", "gametest_grove")
                .climate(0.7F, 0.8F)
                .feature("minecraft:trees_plains")
                .spawn("minecraft:wolf")
                .build());
        var goal = NexusAi.register(NexusAi.GoalLibrary.guardHome("nexuscore", "gametest_guard",
                new NexusAi.HomePosition(new BlockPos(0, 70, 0), 12, true), 12));
        NexusAi.StuckDetector stuckDetector = new NexusAi.StuckDetector(3, 0.5D);

        assertTrue(goal.explain().contains("guard_home"), "Goal explanation should include the goal type");
        assertTrue(stuckDetector.sample(BlockPos.ZERO) == false, "First stuck sample should not trip");
        stuckDetector.sample(BlockPos.ZERO);
        assertTrue(stuckDetector.sample(BlockPos.ZERO), "Repeated samples should trip the stuck detector");
        assertTrue(NexusAi.debugReport().healthy(), "AI debug report should be healthy for registered guard goal");
    }

    public static void v13AutomationRoutesAndSimulatesNetworks() {
        BlockPos source = new BlockPos(0, 64, 0);
        BlockPos relay = new BlockPos(1, 64, 0);
        BlockPos target = new BlockPos(2, 64, 0);
        var network = NexusAutomation.register(NexusAutomation.network("nexuscore", "gametest_network")
                .node(NexusAutomation.TransferNode.item(source, "source"))
                .node(NexusAutomation.TransferNode.item(relay, "relay"))
                .node(NexusAutomation.TransferNode.item(target, "target"))
                .connect(source, relay, NexusAutomation.TransferKind.ITEM, 8)
                .connect(relay, target, NexusAutomation.TransferKind.ITEM, 8)
                .build());

        assertTrue(network.route(source, target, NexusAutomation.TransferKind.ITEM).isPresent(), "Automation route should resolve across relay");
        assertEquals(16, network.simulate().totalThroughput(), "Simulation should total edge throughput");
    }

    public static void v13DataDefinitionsBalanceLiveReloadAndSafetyWorkTogether() {
        var registry = NexusDataDefinitions.registry("nexuscore", "gametest_definitions",
                new JsonSchema().require("type", JsonSchema.Type.STRING),
                json -> json.get("type").getAsString());
        JsonObject valid = new JsonObject();
        valid.addProperty("type", "nexuscore:definition");
        var reload = registry.reload(Map.of(ResourceLocation.fromNamespaceAndPath("nexuscore", "definition"), valid));

        var oldReport = NexusBalance.report("old").metric("energy_per_tick", 10.0D);
        var newReport = NexusBalance.report("new").metric("energy_per_tick", 12.0D);
        NexusLiveReload.watch("gametest", Path.of("generated"), NexusLiveReload.ReloadSafety.developmentOnly());
        var blocked = NexusLiveReload.runReload("gametest", () -> NexusLiveReload.ReloadReport.success("gametest", "ok"), true);

        assertTrue(reload.passed(), "Definition reload should pass for valid JSON");
        assertTrue(NexusBalance.diff(oldReport, newReport).changed(), "Balance diff should detect metric changes");
        assertFalse(blocked.success(), "Development-only reload should be blocked in production mode");
        assertTrue(NexusSafety.teleport().safe(new BlockPos(0, 80, 0), 2), "Teleport safety should accept safe positions");
    }

    public static void v13RecipeAndCompatibilityReportsAreConcrete() {
        var recipe = NexusRecipeFamilies.AdvancedMachineRecipe.builder("nexuscore", "gametest_alloy")
                .itemInput("minecraft:iron_ingot")
                .fluidInput("minecraft:water:250")
                .itemOutput("minecraft:gold_ingot")
                .chanceOutput("minecraft:nugget", 1, 0.25D)
                .energy(500)
                .ticks(80)
                .build();
        NexusRecipeFamilies.register(NexusRecipeFamilies.family("nexuscore", "gametest_family")
                .category("alloying")
                .recipe(recipe)
                .guide(new NexusRecipeFamilies.RecipeGuideIndex(List.of("Alloying"), List.of("alloy", "machine")))
                .build());
        NexusCompatibility.register(NexusCompatibility.integration("jei")
                .loaded(true)
                .recipeViewer("jei")
                .tooltipProvider("jei-runtime-tooltips")
                .mappingDocs("docs/v1.3/compatibility.md")
                .build());

        assertTrue(NexusRecipeFamilies.balanceReport().warnings().isEmpty(), "Balanced recipe should not produce warnings");
        assertEquals(1, NexusCompatibility.matrix().activeCount(), "Compatibility matrix should count loaded integrations");
    }

    public static void v13RuntimeInstallerMaterializesAdvancedSystems() {
        String modId = "nexuscore_runtime_test";
        NexusStructures.registerJigsawPool(NexusStructures.jigsawPoolDefinition(modId, "tower/start_pool")
                .fallback("minecraft:empty")
                .depthLimit(4)
                .element("nexuscore:structures/tower_start", 1, "front")
                .build());
        NexusAi.register(NexusAi.brain(modId, "guard_brain")
                .memory("home", "minecraft:block_pos", 1_200)
                .sensor("nearby_players", 20, "minecraft:player")
                .behavior("return_home", 3, "idle", "home_missing")
                .build());
        NexusDataDefinitions.registerEntity(NexusDataDefinitions.entity(modId, "data_guard")
                .sized(0.6F, 1.9F)
                .tracking(80, 3)
                .goal("nexuscore_runtime_test:guard_brain")
                .property("role", "guard")
                .build());
        JsonObject feature = new JsonObject();
        feature.addProperty("type", "minecraft:no_op");
        NexusDataDefinitions.registerWorldgen(NexusDataDefinitions.worldgen(modId, "crystal_patch")
                .kind("configured_feature")
                .biome("#minecraft:is_overworld")
                .data("worldgen/configured_feature/crystal_patch.json", feature)
                .build());

        NexusDataDefinitions.writeRuntimeDataTo(NexusData.plan(modId));
        var report = NexusRuntimeContent.reports().get("nexuscore");
        var plan = NexusData.plan("nexuscore");

        assertTrue(report != null, "NexusCore runtime content should be installed during bootstrap");
        assertTrue(report.installed().stream().anyMatch(line -> line.equals("automationBlocks=3")), "Runtime installer should register built-in automation blocks");
        assertTrue(report.installed().stream().anyMatch(line -> line.equals("dataDrivenEntities=1")), "Runtime installer should register data-driven entities");
        assertTrue(report.installed().stream().anyMatch(line -> line.equals("dataDrivenWorldgen=2")), "Runtime installer should write entity and worldgen definitions");
        assertTrue(plan.data().containsKey("worldgen/template_pool/runtime/start_pool.json"), "Jigsaw pools should write real template pool JSON");
        assertTrue(plan.data().containsKey("nexuscore/entity_definition/runtime_marker.json"), "Data-driven entity definition should be generated");
        assertTrue(NexusAi.debugReport().brainCount() >= 1, "AI brain definitions should be stable debug-visible registrations");

        var pack = new NexusGeneratedPackResources(new PackLocationInfo(NexusRuntimeRepositorySource.PACK_ID,
                Component.literal("NexusCore Runtime Test"),
                PackSource.BUILT_IN,
                Optional.empty()));
        assertTrue(pack.getResource(PackType.SERVER_DATA, ResourceLocation.fromNamespaceAndPath("nexuscore",
                        "worldgen/template_pool/runtime/start_pool.json")) != null,
                "Runtime pack should expose generated jigsaw pools to dynamic registry loading");
        assertTrue(pack.getResource(PackType.SERVER_DATA, ResourceLocation.fromNamespaceAndPath("nexuscore",
                        "nexuscore/entity_definition/runtime_marker.json")) != null,
                "Runtime pack should expose data-driven entity metadata");
    }

    public static void v13AdvancedCreationSystemsValidateAndGenerateData() {
        String modId = "nexuscore_creation_test";
        var multiblock = NexusMultiblocks.register(NexusMultiblocks.create(modId, "arcane_forge")
                .controller(modId + ":arcane_forge_core")
                .aisle("ABA", "BCB", "ADA")
                .where('A', "minecraft:iron_block")
                .where('B', "minecraft:stone_bricks")
                .where('C', NexusMultiblockPredicates.itemPort())
                .where('D', NexusMultiblockPredicates.energyPort())
                .role('A', NexusMultiblocks.PartRole.CASING)
                .role('C', NexusMultiblocks.PartRole.ITEM_PORT)
                .role('D', NexusMultiblocks.PartRole.ENERGY_PORT)
                .rotatable()
                .mirrorable()
                .machine(NexusMultiblocks.MachineIntegration.processing(modId + ":arcane_forging"))
                .build());
        var scalable = NexusMultiblocks.register(NexusMultiblocks.scalable(modId, "steam_tank")
                .controller(modId + ":steam_tank_controller")
                .frame("minecraft:copper_block")
                .wall("minecraft:glass")
                .interior("minecraft:air")
                .minSize(3, 3, 3)
                .maxSize(5, 5, 5)
                .stat("capacity", ctx -> ctx.innerVolume() * 1000)
                .build());
        var ritual = NexusRituals.register(NexusRituals.create(modId, "storm_calling")
                .center(modId + ":storm_altar")
                .requiresStructure(multiblock.id())
                .requiresItem("minecraft:amethyst_shard", 4)
                .requiresWeather(NexusWeather.ANY)
                .requiresTime(NexusTime.NIGHT)
                .durationSeconds(3)
                .onComplete(NexusRitualActions.setWeather(NexusWeather.THUNDERING))
                .build());
        NexusProgression.register(NexusProgression.node(modId, "arcane_forging")
                .requiresMultiblock(multiblock.id())
                .requiresRitual(ritual.id())
                .unlocksGuidePage(ResourceLocation.fromNamespaceAndPath(modId, "arcane_forge"))
                .build());
        NexusMultiblocks.writeAllTo(NexusData.plan(modId));
        NexusRituals.writeAllTo(NexusData.plan(modId));
        NexusProgression.writeAllTo(NexusData.plan(modId));

        assertTrue(NexusMultiblocks.validate().passed(), "Multiblock definitions should validate");
        assertTrue(NexusRituals.validate().passed(), "Ritual definitions should validate");
        assertTrue(NexusProgression.validate().passed(), "Progression definitions should validate");
        assertTrue(scalable.scalableRules().isPresent(), "Scalable multiblock rules should be retained");
        assertTrue(NexusData.plan(modId).data().containsKey("nexuscore/multiblock/arcane_forge.json"), "Multiblock datagen should write JSON");
        assertTrue(NexusData.plan(modId).data().containsKey("nexuscore/ritual/storm_calling.json"), "Ritual datagen should write JSON");
        assertTrue(NexusData.plan(modId).data().containsKey("nexuscore/progression/arcane_forging.json"), "Progression datagen should write JSON");
    }

    public static void v13AuthoringDataSimulationAndSafetyDescriptorsWork() {
        String modId = "nexuscore_authoring_test";
        var session = NexusAuthoring.openSession("gametest", true, true, false);
        var draft = NexusAuthoring.ritualDraft(ResourceLocation.fromNamespaceAndPath(modId, "ritual_draft"), modId + ":altar", 8);
        NexusDataDefinitions.registerContent(NexusDataDefinitions.content(modId, "data_ritual", NexusDataDefinitions.DefinitionType.RITUAL)
                .property("id", modId + ":data_ritual")
                .property("center", modId + ":altar")
                .guidePage("data_ritual")
                .build());
        var economy = NexusSimulation.economy("gametest_economy")
                .rate("energy_per_tick", 12)
                .recipeChain(modId + ":chain")
                .progressionNode(modId + ":root")
                .run();
        var safetyErrors = NexusSafety.rituals().validate(32, 1, 100, false);

        assertTrue(session.mode() == NexusAuthoring.AuthoringMode.EDIT, "Authoring session should open in development mode");
        assertTrue(draft.json().toString().contains("ritual_draft"), "Ritual draft should export JSON");
        assertTrue(NexusDataDefinitions.validateAll().passed(), "Data-driven content definitions should validate");
        assertTrue(economy.metrics().containsKey("energy_per_tick"), "Economy simulation should include configured rates");
        assertTrue(safetyErrors.isEmpty(), "Safe ritual settings should pass safety validation");
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
