package com.rollylindenshnizzer.nexuscore.example;

import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.client.ClientDescriptor;
import com.rollylindenshnizzer.nexuscore.client.NexusClientDescriptors;
import com.rollylindenshnizzer.nexuscore.component.ComponentDebug;
import com.rollylindenshnizzer.nexuscore.component.NbtToComponentMigration;
import com.rollylindenshnizzer.nexuscore.config.ConfigMigration;
import com.rollylindenshnizzer.nexuscore.config.ConfigPreset;
import com.rollylindenshnizzer.nexuscore.config.NexusConfig;
import com.rollylindenshnizzer.nexuscore.config.OwoConfigBridge;
import com.rollylindenshnizzer.nexuscore.core.CrashHints;
import com.rollylindenshnizzer.nexuscore.core.NexusDiagnostics;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.core.NexusResourcePaths;
import com.rollylindenshnizzer.nexuscore.core.NexusTasks;
import com.rollylindenshnizzer.nexuscore.core.ResourceLocationBuilder;
import com.rollylindenshnizzer.nexuscore.core.TaskQueue;
import com.rollylindenshnizzer.nexuscore.data.DataValidationReport;
import com.rollylindenshnizzer.nexuscore.data.DatagenReportWriters;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.data.NexusDataValidator;
import com.rollylindenshnizzer.nexuscore.debug.CrashHintClassifier;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import com.rollylindenshnizzer.nexuscore.debug.DoctorReport;
import com.rollylindenshnizzer.nexuscore.debug.NexusDoctor;
import com.rollylindenshnizzer.nexuscore.energy.EnergyAccess;
import com.rollylindenshnizzer.nexuscore.energy.NexusEnergyStorage;
import com.rollylindenshnizzer.nexuscore.energy.NexusEnergyTransfer;
import com.rollylindenshnizzer.nexuscore.entity.CombatProfile;
import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinition;
import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinitions;
import com.rollylindenshnizzer.nexuscore.entity.ProjectileDefinition;
import com.rollylindenshnizzer.nexuscore.entity.RegisteredNexusEntity;
import com.rollylindenshnizzer.nexuscore.event.EventDiagnosticRegistry;
import com.rollylindenshnizzer.nexuscore.event.EventTrace;
import com.rollylindenshnizzer.nexuscore.event.NexusEvents;
import com.rollylindenshnizzer.nexuscore.fluid.FluidAccess;
import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import com.rollylindenshnizzer.nexuscore.fluid.NexusFluidTank;
import com.rollylindenshnizzer.nexuscore.fluid.NexusFluidTransfer;
import com.rollylindenshnizzer.nexuscore.inventory.InventoryDropPolicy;
import com.rollylindenshnizzer.nexuscore.inventory.InventorySnapshot;
import com.rollylindenshnizzer.nexuscore.inventory.InventoryTransfer;
import com.rollylindenshnizzer.nexuscore.inventory.SimpleItemHandler;
import com.rollylindenshnizzer.nexuscore.inventory.SlotRange;
import com.rollylindenshnizzer.nexuscore.inventory.TransferRule;
import com.rollylindenshnizzer.nexuscore.loot.LootTableBuilder;
import com.rollylindenshnizzer.nexuscore.machine.MachineRecipeDefinition;
import com.rollylindenshnizzer.nexuscore.machine.MachineScreenLayout;
import com.rollylindenshnizzer.nexuscore.machine.MachineState;
import com.rollylindenshnizzer.nexuscore.machine.NexusMachineDefinition;
import com.rollylindenshnizzer.nexuscore.math.NexusMath;
import com.rollylindenshnizzer.nexuscore.menu.MenuDebugInfo;
import com.rollylindenshnizzer.nexuscore.menu.NexusMenus;
import com.rollylindenshnizzer.nexuscore.network.NetworkMonitor;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import com.rollylindenshnizzer.nexuscore.network.PacketTestHarness;
import com.rollylindenshnizzer.nexuscore.network.RequestResponse;
import com.rollylindenshnizzer.nexuscore.network.SyncBatcher;
import com.rollylindenshnizzer.nexuscore.performance.BatchedBlockUpdates;
import com.rollylindenshnizzer.nexuscore.performance.BenchmarkCase;
import com.rollylindenshnizzer.nexuscore.performance.BenchmarkResult;
import com.rollylindenshnizzer.nexuscore.performance.BenchmarkSuite;
import com.rollylindenshnizzer.nexuscore.performance.ChunkTaskQueue;
import com.rollylindenshnizzer.nexuscore.performance.DirtyFieldTracker;
import com.rollylindenshnizzer.nexuscore.performance.LazyCache;
import com.rollylindenshnizzer.nexuscore.performance.NamedProfiler;
import com.rollylindenshnizzer.nexuscore.performance.NexusBenchmarks;
import com.rollylindenshnizzer.nexuscore.performance.NexusRateLimiter;
import com.rollylindenshnizzer.nexuscore.performance.ReloadAwareMemoizedSupplier;
import com.rollylindenshnizzer.nexuscore.persistence.AttachmentKey;
import com.rollylindenshnizzer.nexuscore.persistence.AttachmentStore;
import com.rollylindenshnizzer.nexuscore.persistence.DataMigrationChain;
import com.rollylindenshnizzer.nexuscore.persistence.NexusCodecs;
import com.rollylindenshnizzer.nexuscore.persistence.NexusNbt;
import com.rollylindenshnizzer.nexuscore.player.PlayerAttachmentSpec;
import com.rollylindenshnizzer.nexuscore.player.PlayerDataStore;
import com.rollylindenshnizzer.nexuscore.registry.ContentModule;
import com.rollylindenshnizzer.nexuscore.registry.CustomRegistrySpec;
import com.rollylindenshnizzer.nexuscore.registry.NexusContentManifest;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistryGroup;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistryReports;
import com.rollylindenshnizzer.nexuscore.resource.DataDrivenRegistry;
import com.rollylindenshnizzer.nexuscore.resource.JsonSchema;
import com.rollylindenshnizzer.nexuscore.resource.ResourceValidationReport;
import com.rollylindenshnizzer.nexuscore.resource.TypedDataLoader;
import com.rollylindenshnizzer.nexuscore.security.ClientActionGuard;
import com.rollylindenshnizzer.nexuscore.security.SafePaths;
import com.rollylindenshnizzer.nexuscore.sound.NexusSounds;
import com.rollylindenshnizzer.nexuscore.tag.NexusTags;
import com.rollylindenshnizzer.nexuscore.ui.HudOverlayRegistry;
import com.rollylindenshnizzer.nexuscore.ui.MiniMarkup;
import com.rollylindenshnizzer.nexuscore.ui.NexusUi;
import com.rollylindenshnizzer.nexuscore.ui.RichTextBuilder;
import com.rollylindenshnizzer.nexuscore.ui.WidgetDescriptor;
import com.rollylindenshnizzer.nexuscore.ui.WidgetLibrary;
import com.rollylindenshnizzer.nexuscore.ui.binding.MachineUiBindings;
import com.rollylindenshnizzer.nexuscore.ui.binding.ObservableValue;
import com.rollylindenshnizzer.nexuscore.ui.form.FormBuilder;
import com.rollylindenshnizzer.nexuscore.ui.nav.ScreenRouter;
import com.rollylindenshnizzer.nexuscore.world.NexusWorlds;
import com.rollylindenshnizzer.nexuscore.world.StructureHelpers;
import com.rollylindenshnizzer.nexuscore.world.TeleportTargetBuilder;
import com.rollylindenshnizzer.nexuscore.worldgen.BiomeSelector;
import com.rollylindenshnizzer.nexuscore.worldgen.NexusWorldgen;
import com.rollylindenshnizzer.nexuscore.worldgen.OreGenerationBuilder;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NexusCoreExampleSystems {
    private static final Map<String, String> SNAPSHOT = new LinkedHashMap<>();
    private static RegisteredNexusEntity<AreaEffectCloud> markerEntity;
    private static RegistrySupplier<MenuType<ChestMenu>> tutorialMenu;
    private static CustomRegistrySpec<ExampleDatapackRecipe> rubyTraitRegistry;
    private static ResourceValidationReport datapackReport;
    private static List<BenchmarkResult> benchmarkResults = List.of();

    public static List<ContentModule> contentModules() {
        return List.of(new ExampleModule("materials", List.of()), new ExampleModule("machines", List.of("materials")));
    }

    public static void registerBeforeRegistries(RegistrySupplier<CreativeModeTab> tab,
                                                RegistrySupplier<Block> rubyOre) {
        NexusRegistries.group(NexusCoreExampleContent.MOD_ID)
                .translationPrefix("nexuscore_example")
                .assetPathPrefix("tutorial")
                .datagenDefaults("generated")
                .tag("example")
                .validationRule("no duplicate generated assets");

        tutorialMenu = NexusMenus.menu(NexusCoreExampleContent.MOD_ID, "tutorial_chest_menu", ChestMenu::threeRows);

        NexusSounds.variable(NexusCoreExampleContent.MOD_ID, "ruby_press/start");
        NexusSounds.fixed(NexusCoreExampleContent.MOD_ID, "ruby_press/finish", 16.0F);
        NexusSounds.subtitle(NexusCoreExampleContent.MOD_ID, "ruby_press/start", "Ruby press starts");
        NexusSounds.subtitle(NexusCoreExampleContent.MOD_ID, "ruby_press/finish", "Ruby press finishes");

        NexusClientDescriptors.register(new ClientDescriptor.Keybind("ruby_debug",
                "key.nexuscore_example.ruby_debug", "key.categories.nexuscore_example", 82));
        NexusClientDescriptors.register(new ClientDescriptor.DebugLayer("ruby_machine_state", "Ruby Machine State", true));
        NexusClientDescriptors.register(new ClientDescriptor.RenderLayer("ruby_ore_cutout", id("ruby_ore"), "cutout"));
        NexusClientDescriptors.register(new ClientDescriptor.ColorProvider("ruby_tint", id("ruby"), 0xE43757));
        NexusClientDescriptors.register(new ClientDescriptor.ParticleEffect("ruby_spark", id("ruby_spark"), 0xE43757, 0.25F));

        NexusEntityDefinition markerDefinition = NexusEntityDefinitions.entity(NexusCoreExampleContent.MOD_ID,
                        "ruby_marker", MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .tracking(32, 10)
                .attribute("lifetime_ticks", 80.0)
                .projectile(ProjectileDefinition.simple(2.0, 1.5F))
                .build();
        markerEntity = NexusEntityDefinitions.registerType(markerDefinition, AreaEffectCloud::new);

        rubyTraitRegistry = CustomRegistrySpec.builder(id("ruby_traits"), ExampleDatapackRecipe.class)
                .synced()
                .build();

        TagKey<Block> rubyOres = NexusTags.block(NexusCoreExampleContent.MOD_ID, "ores/ruby");
        TagKey<Item> rubyGems = NexusTags.item(NexusCoreExampleContent.MOD_ID, "gems/ruby");
        SNAPSHOT.put("registry.group", NexusRegistries.group(NexusCoreExampleContent.MOD_ID).modId());
        SNAPSHOT.put("registry.menu", "tutorial_chest_menu");
        SNAPSHOT.put("tags", rubyOres.location() + ", " + rubyGems.location());
        SNAPSHOT.put("client.descriptors", Integer.toString(NexusClientDescriptors.descriptors().size()));
        SNAPSHOT.put("entity.marker", markerDefinition.id().toString());
        SNAPSHOT.put("custom.registry", rubyTraitRegistry.id().toString());
        SNAPSHOT.put("resource.model", NexusResourcePaths.blockModel(NexusCoreExampleContent.MOD_ID, rubyOre.getId().getPath()).toString());
    }

    public static void populateGeneratedData(NexusData.DataPlan plan) {
        plan.translation("key.categories.nexuscore_example", "NexusCore Example")
                .translation("key.nexuscore_example.ruby_debug", "Open Ruby Debug")
                .translation("config.nexuscore_example.enable_particles", "Enable Ruby Particles")
                .translation("config.nexuscore_example.workbench_label", "Ruby Workbench Label")
                .translation("config.nexuscore_example.balance_mode", "Balancing Mode")
                .translation("entity.nexuscore_example.ruby_marker", "Ruby Marker")
                .translation("subtitles.nexuscore_example.ruby_press.start", "Ruby press starts")
                .translation("subtitles.nexuscore_example.ruby_press.finish", "Ruby press finishes")
                .tag("items", "gems/ruby", NexusCoreExampleContent.MOD_ID + ":ruby")
                .tag("blocks", "ores/ruby", NexusCoreExampleContent.MOD_ID + ":ruby_ore")
                .data("loot_table/tutorial/ruby_cache.json", LootTableBuilder.block()
                        .selfDrop(NexusCoreExampleContent.MOD_ID + ":ruby")
                        .explosionSurvives()
                        .build());

        JsonObject datapackRecipe = new JsonObject();
        datapackRecipe.addProperty("name", "polished_ruby");
        datapackRecipe.addProperty("energy", 120);
        datapackRecipe.addProperty("requires_water", true);
        plan.data("ruby_traits/polished_ruby.json", datapackRecipe);
    }

    public static void installRuntimeExamples(NexusConfig config,
                                              NexusEnergyStorage energy,
                                              NexusFluidTank tank,
                                              SimpleItemHandler machineInventory,
                                              MachineState rubyPressState,
                                              NexusMachineDefinition rubyPressDefinition,
                                              MachineRecipeDefinition rubyPressRecipe,
                                              RegistrySupplier<DataComponentType<String>> modeComponent) {
        demonstrateCoreAndDiagnostics();
        demonstrateConfig(config);
        demonstrateDataAndResources();
        demonstrateEvents();
        demonstrateTransfers();
        demonstrateInventory(machineInventory);
        demonstrateMachineUi(energy, tank, rubyPressState, rubyPressDefinition, rubyPressRecipe);
        demonstrateComponents(modeComponent);
        demonstrateNetworking();
        demonstratePersistence();
        demonstratePlayerWorldSecurity();
        demonstrateUi(config, energy, tank, rubyPressState);
        demonstratePerformance();

        DebugRegistry.section("nexuscore_example.systems", () -> SNAPSHOT.toString());
        DebugRegistry.section("nexuscore_example.datapack_loader", () -> datapackReport == null ? "not run" : datapackReport.summary());
        DebugRegistry.section("nexuscore_example.benchmarks", () -> BenchmarkSuite.toMarkdown(benchmarkResults).replace('\n', ' '));
        DebugRegistry.section("nexuscore_example.registry_report", () ->
                NexusRegistryReports.report(NexusRegistries.group(NexusCoreExampleContent.MOD_ID)).summary());
        DebugRegistry.section("nexuscore_example.menu", () ->
                tutorialMenu == null ? "not registered" : "tutorial_chest_menu");
        DebugRegistry.section("nexuscore_example.entity", () ->
                markerEntity == null ? "not registered" : markerEntity.definition().id().toString());
    }

    private static void demonstrateCoreAndDiagnostics() {
        CrashHints.register("nexuscore_example_missing_texture", "Check generated item and block model paths.");
        NexusDiagnostics.Report startup = NexusDiagnostics.startup(NexusCoreExampleContent.MOD_ID);
        ResourceLocation generated = new ResourceLocationBuilder(NexusCoreExampleContent.MOD_ID)
                .path("generated")
                .path("ruby")
                .suffix("preview")
                .build();
        NexusTasks.queue(TaskQueue.SERVER_STARTED, () -> EventTrace.record("task", "server-started task queued"));
        SNAPSHOT.put("core.id", NexusIds.translationKey("item", id("ruby")));
        SNAPSHOT.put("core.generated_id", generated.toString());
        SNAPSHOT.put("core.diagnostics", startup.summary());
        SNAPSHOT.put("core.crash_hint", CrashHints.explain(new IllegalStateException("nexuscore_example_missing_texture")).orElse("none"));
    }

    private static void demonstrateConfig(NexusConfig config) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("machine_energy_cost", 80);
        ConfigPreset preset = ConfigPreset.builder("starter")
                .description("Fast tutorial machine settings")
                .icon(NexusCoreExampleContent.MOD_ID + ":ruby")
                .value("machine_energy_cost", 80)
                .value("enable_particles", true)
                .build();
        ConfigMigration migration = new ConfigMigration(1, 2, map ->
                map.putIfAbsent("workbench_label", "Ruby Workbench"));
        migration.apply(values);
        SNAPSHOT.put("config.options", config.options().keySet().toString());
        SNAPSHOT.put("config.preset", preset.id() + "=" + preset.values());
        SNAPSHOT.put("config.migration", values.toString());
        SNAPSHOT.put("config.owo", OwoConfigBridge.status());
    }

    private static void demonstrateDataAndResources() {
        JsonObject raw = new JsonObject();
        raw.addProperty("name", "polished_ruby");
        raw.addProperty("energy", 120);
        raw.addProperty("requires_water", true);

        TypedDataLoader<ExampleDatapackRecipe> loader = new TypedDataLoader<>("ruby_traits",
                new JsonSchema()
                        .require("name", JsonSchema.Type.STRING)
                        .require("energy", JsonSchema.Type.NUMBER)
                        .optional("requires_water", JsonSchema.Type.BOOLEAN),
                json -> new ExampleDatapackRecipe(json.get("name").getAsString(),
                        json.get("energy").getAsInt(),
                        json.has("requires_water") && json.get("requires_water").getAsBoolean()));
        DataDrivenRegistry<ExampleDatapackRecipe> registry = new DataDrivenRegistry<>(loader);
        datapackReport = registry.reload(Map.of(id("polished_ruby"), raw));

        DataValidationReport validation = NexusDataValidator.validatePlan(NexusData.plan(NexusCoreExampleContent.MOD_ID));
        SNAPSHOT.put("data.validation", validation.summary());
        SNAPSHOT.put("data.validation.markdown", DatagenReportWriters.toMarkdown(validation).lines().findFirst().orElse(""));
        SNAPSHOT.put("resource.registry", registry.values().toString());
        SNAPSHOT.put("resource.loader_folder", loader.folder());
    }

    private static void demonstrateEvents() {
        NexusEvents.serverTick(server -> {
            if (server.getTickCount() % 200 == 0) {
                EventTrace.record("server_tick", "example heartbeat " + server.getTickCount());
            }
        });
        EventDiagnosticRegistry.mark("example_manual_event");
        EventTrace.record("bootstrap", "runtime examples installed");
        SNAPSHOT.put("events", EventDiagnosticRegistry.snapshot().toString());
    }

    private static void demonstrateTransfers() {
        NexusEnergyStorage source = NexusEnergyStorage.builder(1_000)
                .io(250, 250)
                .side(Direction.SOUTH, EnergyAccess.OUTPUT)
                .build();
        NexusEnergyStorage target = NexusEnergyStorage.builder(1_000)
                .io(250, 250)
                .side(Direction.NORTH, EnergyAccess.INPUT)
                .build();
        source.insert(500, false);
        NexusEnergyTransfer.Result energyResult = NexusEnergyTransfer.move(source, Direction.SOUTH, target, Direction.NORTH, 200, false);

        NexusFluidTank water = NexusFluidTank.builder(1_000).side(Direction.SOUTH, FluidAccess.OUTPUT).build();
        NexusFluidTank buffer = NexusFluidTank.builder(1_000).side(Direction.NORTH, FluidAccess.INPUT).build();
        water.fill(new FluidStack(Fluids.WATER, 500), false);
        NexusFluidTransfer.Result fluidResult = NexusFluidTransfer.move(water, Direction.SOUTH, buffer, Direction.NORTH, 250, false);

        SNAPSHOT.put("transfer.energy", energyResult.moved() + " " + energyResult.trace());
        SNAPSHOT.put("transfer.fluid", fluidResult.moved().amount() + " " + fluidResult.trace());
    }

    private static void demonstrateInventory(SimpleItemHandler machineInventory) {
        SimpleItemHandler demo = new SimpleItemHandler(4);
        demo.set(0, new ItemStack(Items.REDSTONE, 8));
        InventorySnapshot before = InventorySnapshot.capture(demo);
        TransferRule rule = TransferRule.builder("input_to_output", new SlotRange(0, 1), new SlotRange(1, 4))
                .maxPerOperation(4)
                .build();
        var transfer = InventoryTransfer.route(demo, 0, TransferRule.TransferActor.PLAYER, List.of(rule), false);
        InventorySnapshot after = InventorySnapshot.capture(demo);

        SNAPSHOT.put("inventory.transfer", transfer.moved() + " moved, trace=" + transfer.trace());
        SNAPSHOT.put("inventory.diff", before.diff(after).toString());
        SNAPSHOT.put("inventory.drop_policy", InventoryDropPolicy.DROP_OUTPUTS_ONLY.name());
        SNAPSHOT.put("inventory.machine_size", Integer.toString(machineInventory.size()));
    }

    private static void demonstrateMachineUi(NexusEnergyStorage energy,
                                             NexusFluidTank tank,
                                             MachineState state,
                                             NexusMachineDefinition definition,
                                             MachineRecipeDefinition recipe) {
        MachineScreenLayout layout = MachineScreenLayout.generated(definition);
        MachineUiBindings bindings = MachineUiBindings.machine(state, energy, tank);
        SNAPSHOT.put("machine.definition", definition.id() + " slots=" + definition.inventorySize());
        SNAPSHOT.put("machine.recipe", recipe.id() + " ticks=" + recipe.processingTicks());
        SNAPSHOT.put("machine.layout", layout.widgets().toString());
        SNAPSHOT.put("machine.bindings", bindings.values().keySet().toString());
    }

    private static void demonstrateComponents(RegistrySupplier<DataComponentType<String>> modeComponent) {
        ItemStack before = new ItemStack(Items.STICK);
        ItemStack after = before.copy();
        after.set(modeComponent.get(), "charged");

        CompoundTag legacy = new CompoundTag();
        legacy.putString("LegacyMode", "legacy_charged");
        NbtToComponentMigration<String> migration = new NbtToComponentMigration<>(
                "LegacyMode", modeComponent.get(), (tag, stack) -> tag.getString("LegacyMode"));
        int migrated = new NbtToComponentMigration.Suite().add(migration).migrate(after, legacy);

        SNAPSHOT.put("component.diff", ComponentDebug.diff(before, after).toString());
        SNAPSHOT.put("component.pretty", ComponentDebug.prettyPrint(after).replace('\n', ' '));
        SNAPSHOT.put("component.migrated", Integer.toString(migrated));
        SNAPSHOT.put("component.codec", NexusCodecs.enumCodec(ExampleMode.class).toString());
    }

    private static void demonstrateNetworking() {
        NexusNetworking.ChannelBuilder channel = NexusNetworking.channel(NexusCoreExampleContent.MOD_ID, "diagnostics")
                .protocolVersion("1.2")
                .disconnectOnMismatch((client, server) ->
                        "Example diagnostic channel mismatch: client=" + client + ", server=" + server);
        ResourceLocation ping = channel.id("ping");
        NetworkMonitor.record(ping, 12, null);

        RequestResponse<String> requestResponse = new RequestResponse<>();
        UUID request = requestResponse.begin(Duration.ofSeconds(5));
        requestResponse.success(request, "accepted");

        SyncBatcher<ResourceLocation> batcher = new SyncBatcher<>(Duration.ZERO);
        List<ResourceLocation> flushed = new ArrayList<>();
        batcher.markDirty(id("ruby_press"));
        batcher.flushIfReady(flushed::addAll);

        PacketTestHarness.assertRoundTrip(new ExamplePacket(42),
                (buffer, packet) -> buffer.writeInt(packet.value()),
                buffer -> new ExamplePacket(buffer.readInt()));

        SNAPSHOT.put("network.channel", NexusNetworking.diagnostics().get(id("diagnostics")).toString());
        SNAPSHOT.put("network.monitor", NetworkMonitor.snapshot().toString());
        SNAPSHOT.put("network.request", requestResponse.poll(request).map(RequestResponse.Result::response).orElse("pending"));
        SNAPSHOT.put("network.batcher", flushed.toString());
    }

    private static void demonstratePersistence() {
        AttachmentKey<Integer> charge = new AttachmentKey<>(id("ruby_charge"), Integer.class,
                AttachmentKey.SyncPolicy.OWNER, AttachmentKey.CopyPolicy.ON_DEATH);
        AttachmentStore source = new AttachmentStore();
        AttachmentStore target = new AttachmentStore();
        source.put(charge, 64);
        source.copyTo(target, AttachmentKey.CopyPolicy.ON_DEATH);

        CompoundTag data = new CompoundTag();
        data.putInt("version", 1);
        DataMigrationChain<CompoundTag> chain = new DataMigrationChain<CompoundTag>()
                .step(1, 2, tag -> {
                    CompoundTag copy = tag.copy();
                    copy.putInt("version", 2);
                    copy.putString("mode", "migrated");
                    return copy;
                });
        CompoundTag migrated = chain.migrate(1, 2, data);

        PlayerDataStore playerData = new PlayerDataStore();
        UUID first = UUID.nameUUIDFromBytes("first".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        UUID second = UUID.nameUUIDFromBytes("second".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        playerData.get(first).putInt("ruby_charge", 64);
        playerData.copy(first, second);

        PlayerAttachmentSpec<Integer> playerAttachment = new PlayerAttachmentSpec<>(id("ruby_charge"),
                0, PlayerAttachmentSpec.SyncPolicy.OWNER_ONLY, true, true);

        SNAPSHOT.put("persistence.attachment", target.get(charge).map(Object::toString).orElse("missing"));
        SNAPSHOT.put("persistence.migration", NexusNbt.string(migrated, "mode").orElse("missing"));
        SNAPSHOT.put("persistence.player", Integer.toString(playerData.get(second).getInt("ruby_charge")));
        SNAPSHOT.put("persistence.player_spec", playerAttachment.toString());
    }

    private static void demonstratePlayerWorldSecurity() {
        ClientActionGuard<ExampleClientAction> guard = new ClientActionGuard<ExampleClientAction>()
                .allow("positive_amount", action -> action.amount() > 0)
                .allow("known_target", action -> action.target().getNamespace().equals(NexusCoreExampleContent.MOD_ID));
        ClientActionGuard.Result guardResult = guard.validate(new ExampleClientAction(id("ruby_press"), 1));

        Path reportPath = SafePaths.validateExport(Path.of("build", "nexuscore-example"), "reports/example.md");
        long positions = NexusWorlds.radius(BlockPos.ZERO, 1).count();
        BlockPos structureCenter = StructureHelpers.center(new BoundingBox(0, 0, 0, 8, 6, 8));
        TeleportTargetBuilder target = new TeleportTargetBuilder(Level.OVERWORLD)
                .position(new Vec3(0.5, 80.0, 0.5))
                .rotation(Vec2.ZERO);
        CombatProfile combatProfile = new CombatProfile(id("ruby_guardian"), 4.0, 1.2, 6.0, 2.0,
                0.15, Map.of(EquipmentSlot.MAINHAND, id("entities/ruby_guardian_mainhand")));

        SNAPSHOT.put("security.guard", guardResult.reason());
        SNAPSHOT.put("security.safe_path", reportPath.toString());
        SNAPSHOT.put("world.radius_positions", Long.toString(positions));
        SNAPSHOT.put("world.structure_center", structureCenter.toShortString());
        SNAPSHOT.put("world.teleport_target", target.toString());
        SNAPSHOT.put("entity.combat", combatProfile.toString());
    }

    private static void demonstrateUi(NexusConfig config,
                                      NexusEnergyStorage energy,
                                      NexusFluidTank tank,
                                      MachineState state) {
        FormBuilder form = FormBuilder.create()
                .bind(config)
                .onSubmit(values -> EventTrace.record("form", values.keySet().toString()));
        form.field("runtime_note", "ready").validator(value -> !value.isBlank(), "note cannot be blank");
        FormBuilder.Result formResult = form.submit();

        ObservableValue<String> status = new ObservableValue<>("idle");
        status.onChanged(value -> EventTrace.record("observable", value));
        status.set("ready");

        ScreenRouter<String> router = new ScreenRouter<>(screen -> EventTrace.record("screen", screen));
        router.open("machine");
        router.open("config");
        router.back();

        NexusUi.ScreenSpec screen = NexusUi.screen(Component.literal("Ruby Diagnostics"))
                .add(NexusUi.button("open_machine", Component.literal("Machine")).bounds(8, 8, 80, 20))
                .add(NexusUi.itemStack("ruby_preview", new ItemStack(Items.DIAMOND)).bounds(8, 36, 18, 18));
        WidgetLibrary.Widget energyBar = WidgetLibrary.energyBar("ruby_energy", energy.amount(), energy.capacity());
        WidgetLibrary.Widget fluidTank = WidgetLibrary.fluidTank("ruby_fluid", Fluids.WATER.builtInRegistryHolder().key().location(),
                tank.stored().amount(), tank.capacity());
        WidgetDescriptor registryPicker = WidgetDescriptor.registryPicker("machine.recipe", "minecraft:recipe_serializer");
        MachineUiBindings bindings = MachineUiBindings.machine(state, energy, tank);
        HudOverlayRegistry.register("nexuscore_example.machine_status", HudOverlayRegistry.Anchor.TOP_RIGHT, () -> true);

        SNAPSHOT.put("ui.form", Boolean.toString(formResult.successful()));
        SNAPSHOT.put("ui.screen", screen.widgets().toString());
        SNAPSHOT.put("ui.widgets", energyBar + " " + fluidTank + " " + registryPicker);
        SNAPSHOT.put("ui.bindings", bindings.values().keySet().toString());
        SNAPSHOT.put("ui.router", router.breadcrumbs().display());
        SNAPSHOT.put("ui.text", new RichTextBuilder().literal("Ruby ").translatable("item.nexuscore_example.ruby").build().getString());
        SNAPSHOT.put("ui.markup", MiniMarkup.parse("[red]Ruby [white]Press").getString());
    }

    private static void demonstratePerformance() {
        DirtyFieldTracker dirty = new DirtyFieldTracker();
        dirty.mark("energy");
        LazyCache<Integer> lazy = new LazyCache<>(() -> 7);
        ReloadAwareMemoizedSupplier<String> memoized = new ReloadAwareMemoizedSupplier<>(() -> "reloadable");
        NexusRateLimiter limiter = new NexusRateLimiter(Duration.ofMillis(1));

        ChunkTaskQueue queue = new ChunkTaskQueue();
        queue.queue(new ChunkPos(0, 0), () -> EventTrace.record("chunk_task", "ran"));
        int ran = queue.run(new ChunkPos(0, 0), 4);

        BatchedBlockUpdates blockUpdates = new BatchedBlockUpdates();
        blockUpdates.mark(BlockPos.ZERO);

        try (NamedProfiler.Section ignored = NamedProfiler.global().section("example.runtime_setup")) {
            NexusMath.clamp(12, 0, 10);
        }
        NexusMath.WeightedTable<String> table = new NexusMath.WeightedTable<String>()
                .add("ruby", 4)
                .add("raw_ruby", 1);
        String picked = table.pick(RandomSource.create(1L));

        NexusBenchmarks.register(BenchmarkCase.of("example-cache", 4, lazy::get));
        benchmarkResults = new BenchmarkSuite()
                .add("example-profiler", 4, () -> {
                    try (NamedProfiler.Section ignored = NamedProfiler.global().section("example.benchmark")) {
                        Math.sqrt(81.0);
                    }
                })
                .run();

        SNAPSHOT.put("performance.dirty", dirty.flush().toString());
        SNAPSHOT.put("performance.lazy", lazy.get().toString());
        SNAPSHOT.put("performance.memoized", memoized.get());
        SNAPSHOT.put("performance.limiter", Boolean.toString(limiter.tryAcquire("ruby_press")));
        SNAPSHOT.put("performance.chunk_tasks", Integer.toString(ran));
        SNAPSHOT.put("performance.weighted_pick", picked);
        SNAPSHOT.put("performance.profiler", NamedProfiler.global().top(3).toString());
    }

    private static ResourceLocation id(String path) {
        return NexusIds.id(NexusCoreExampleContent.MOD_ID, path);
    }

    public record ExampleDatapackRecipe(String name, int energy, boolean requiresWater) {
    }

    private record ExamplePacket(int value) {
    }

    private record ExampleClientAction(ResourceLocation target, int amount) {
    }

    public enum ExampleMode {
        ECONOMY,
        BALANCED,
        PERFORMANCE
    }

    private record ExampleModule(String id, List<String> dependencies) implements ContentModule {
        @Override
        public void register(NexusRegistryGroup registries) {
            NexusContentManifest.record(NexusCoreExampleContent.MOD_ID, "module", "content_module", id,
                    NexusContentManifest.sourceHint(), "example");
        }

        @Override
        public void dataGeneration() {
            NexusData.plan(NexusCoreExampleContent.MOD_ID)
                    .translation("module.nexuscore_example." + id, "Example module: " + id);
        }

        @Override
        public void compatibility() {
            EventTrace.record("module", id + " compatibility hook");
        }
    }

    private NexusCoreExampleSystems() {
    }
}
