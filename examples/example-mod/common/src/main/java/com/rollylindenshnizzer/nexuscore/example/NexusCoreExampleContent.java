package com.rollylindenshnizzer.nexuscore.example;

import com.rollylindenshnizzer.nexuscore.advancement.AdvancementJsonBuilder;
import com.rollylindenshnizzer.nexuscore.block.NexusBlockSet;
import com.rollylindenshnizzer.nexuscore.block.NexusBlockSets;
import com.rollylindenshnizzer.nexuscore.block.NexusBlocks;
import com.rollylindenshnizzer.nexuscore.command.NexusCommands;
import com.rollylindenshnizzer.nexuscore.command.NexusDebugCommands;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerBridge;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerCategory;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerDisplay;
import com.rollylindenshnizzer.nexuscore.component.NexusComponents;
import com.rollylindenshnizzer.nexuscore.config.ConfigSchemaExporter;
import com.rollylindenshnizzer.nexuscore.config.NexusConfig;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.core.NexusLifecycle;
import com.rollylindenshnizzer.nexuscore.core.NexusMod;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.data.RecipeJsonBuilder;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import com.rollylindenshnizzer.nexuscore.energy.EnergyStorage;
import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import com.rollylindenshnizzer.nexuscore.fluid.FluidTank;
import com.rollylindenshnizzer.nexuscore.inventory.QuickMoveRouter;
import com.rollylindenshnizzer.nexuscore.inventory.SlotRange;
import com.rollylindenshnizzer.nexuscore.item.NexusItem;
import com.rollylindenshnizzer.nexuscore.item.NexusItems;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import com.rollylindenshnizzer.nexuscore.performance.CooldownTracker;
import com.rollylindenshnizzer.nexuscore.performance.ExpiringCache;
import com.rollylindenshnizzer.nexuscore.test.ValidationSuite;
import com.rollylindenshnizzer.nexuscore.worldgen.OreFeatureJsonBuilder;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class NexusCoreExampleContent extends NexusMod {
    public static final String MOD_ID = "nexuscore_example";
    private static final NexusCoreExampleContent INSTANCE = new NexusCoreExampleContent();

    private final ExampleConfig config = new ExampleConfig();
    private final EnergyStorage energy = new EnergyStorage(10_000, 250, 250);
    private final FluidTank tank = new FluidTank(4_000);
    private final CooldownTracker<String> cooldowns = new CooldownTracker<>();
    private final ExpiringCache<Integer> registeredItemCount = new ExpiringCache<>(Duration.ofMinutes(1), () -> 3);
    private RegistrySupplier<NexusItem> ruby;
    private RegistrySupplier<NexusItem> rawRuby;
    private RegistrySupplier<NexusItem> rubyApple;
    private RegistrySupplier<Block> rubyBlock;
    private RegistrySupplier<Block> rubyOre;
    private RegistrySupplier<DataComponentType<String>> modeComponent;
    private NexusBlockSet sapphireSet;

    private NexusCoreExampleContent() {
        super(MOD_ID);
    }

    public static void bootstrap() {
        INSTANCE.init();
    }

    public static NexusData.DataPlan populateGeneratedData() {
        return NexusData.plan(MOD_ID)
                .translation("itemGroup.nexuscore_example.main", "NexusCore Example")
                .translation("tooltip.nexuscore_example.ruby", "Generated with NexusCore item helpers")
                .translation("tooltip.nexuscore_example.raw_ruby", "Smelts into a ruby")
                .translation("advancements.nexuscore_example.root.title", "NexusCore Example")
                .translation("advancements.nexuscore_example.root.description", "Pick up a ruby")
                .translation("recipe_viewer.nexuscore_example.ruby_workbench", "Ruby Workbench")
                .data("recipe/ruby_block.json", RecipeJsonBuilder.shaped("building", MOD_ID + ":ruby_block", 1)
                        .pattern("RRR", "RRR", "RRR")
                        .key('R', MOD_ID + ":ruby")
                        .build())
                .data("recipe/ruby_from_block.json", RecipeJsonBuilder.shapeless("misc", MOD_ID + ":ruby", 9)
                        .ingredient(MOD_ID + ":ruby_block")
                        .build())
                .data("recipe/ruby_from_smelting.json", RecipeJsonBuilder.cooking("minecraft:smelting", "misc",
                        MOD_ID + ":raw_ruby", MOD_ID + ":ruby", 0.7F, 200).build())
                .data("worldgen/configured_feature/ruby_ore.json", new OreFeatureJsonBuilder()
                        .targetTag("minecraft:stone_ore_replaceables", MOD_ID + ":ruby_ore")
                        .size(6)
                        .discardChanceOnAirExposure(0.35F)
                        .buildConfiguredFeature())
                .data("advancement/root.json", new AdvancementJsonBuilder()
                        .display(MOD_ID + ":ruby", "advancements.nexuscore_example.root.title",
                                "advancements.nexuscore_example.root.description", "task", true, false, false)
                        .criterion("has_ruby", AdvancementJsonBuilder.inventoryChanged(MOD_ID + ":ruby"))
                        .build());
    }

    @Override
    protected void beforeRegistries() {
        RegistrySupplier<CreativeModeTab> tab = NexusItems.creativeTab(MOD_ID, "main")
                .icon(() -> new ItemStack(Items.DIAMOND))
                .register();

        ruby = NexusItems.item(MOD_ID, "ruby")
                .creativeTab(tab)
                .tooltip("tooltip.nexuscore_example.ruby")
                .modelGenerated()
                .defaultColor(0xE43757)
                .register();

        rawRuby = NexusItems.item(MOD_ID, "raw_ruby")
                .creativeTab(tab)
                .tooltip("tooltip.nexuscore_example.raw_ruby")
                .modelGenerated()
                .fuel(200)
                .register();

        rubyApple = NexusItems.item(MOD_ID, "ruby_apple")
                .creativeTab(tab)
                .food(NexusItems.food().nutrition(6).saturation(0.8F).alwaysEdible().build())
                .modelGenerated()
                .register();

        rubyBlock = NexusBlocks.block(MOD_ID, "ruby_block")
                .strength(5.0F, 6.0F)
                .requiresCorrectTool()
                .mapColor(MapColor.COLOR_RED)
                .withBlockItem()
                .creativeTab(tab)
                .simpleCubeModel()
                .dropsSelf()
                .mineableWithPickaxe()
                .needsIronTool()
                .defaultColor(0xC82248)
                .register();

        rubyOre = NexusBlocks.block(MOD_ID, "ruby_ore")
                .strength(3.0F, 3.0F)
                .requiresCorrectTool()
                .mapColor(MapColor.STONE)
                .withBlockItem()
                .creativeTab(tab)
                .simpleCubeModel()
                .dropsSelf()
                .mineableWithPickaxe()
                .needsIronTool()
                .register();

        modeComponent = NexusComponents.<String>item(MOD_ID, "mode")
                .codec(Codec.STRING)
                .streamCodec(ByteBufCodecs.STRING_UTF8)
                .networkSynced()
                .defaultValue("idle")
                .cacheable()
                .tooltip((mode, tooltip) -> tooltip.add(Component.literal("Mode: " + mode)))
                .register();

        sapphireSet = NexusBlockSets.gem(MOD_ID, "sapphire")
                .material(MapColor.COLOR_BLUE)
                .strength(4.0F, 5.0F)
                .creativeTab(tab)
                .generateRecipes()
                .generateTags()
                .register();

        populateGeneratedData();
    }

    @Override
    protected void onInitialize() {
        config.validateAll();
        energy.insert(config.machineEnergyCost.get(), false);
        tank.fill(new FluidStack(Fluids.WATER, 1_000), false);
        cooldowns.set("ruby_press", 0, 20);

        NexusDebugCommands.install(MOD_ID);
        NexusCommands.literal("nexuscoreexample")
                .permission(0)
                .feedback(Component.literal("NexusCore example command registered"))
                .register();

        NexusNetworking.channel(MOD_ID, "main")
                .protocolVersion("1.1")
                .disconnectOnMismatch((client, server) -> "NexusCore example protocol mismatch: client " + client + ", server " + server);
        ConfigSchemaExporter.jsonSchema(config, "1.1");

        /*
         * Do not call RegistrySupplier#get() directly during mod construction /
         * early initialization. On NeoForge, the deferred registry entries may
         * not be present yet.
         *
         * The recipe viewer content creates ItemStacks from registered objects,
         * so it is deferred until COMMON_SETUP.
         */
        NexusLifecycle.on(NexusLifecycle.Phase.COMMON_SETUP, this::registerRecipeViewerContent);

        new QuickMoveRouter()
                .route(new SlotRange(0, 1), new SlotRange(1, 37), stack -> true)
                .route(new SlotRange(1, 37), new SlotRange(0, 1), stack -> true);

        ValidationSuite.Result validation = new ValidationSuite()
                .check("energy seeded", () -> {
                    if (energy.amount() <= 0) {
                        throw new AssertionError("Expected seeded example energy");
                    }
                })
                .check("fluid seeded", () -> {
                    if (tank.stored().amount() <= 0) {
                        throw new AssertionError("Expected seeded example fluid");
                    }
                })
                .run();

        DebugRegistry.section("nexuscore_example.items", () -> Integer.toString(registeredItemCount.get()));
        DebugRegistry.section("nexuscore_example.blocks", () -> Integer.toString(2));
        DebugRegistry.section("nexuscore_example.block_set", () -> sapphireSet.blocks().keySet().toString());
        DebugRegistry.section("nexuscore_example.component", () -> MOD_ID + ":mode");
        DebugRegistry.section("nexuscore_example.energy_cost", () -> Integer.toString(config.machineEnergyCost.get()));
        DebugRegistry.section("nexuscore_example.validation", () -> validation.passed() ? "passed" : validation.failures().toString());
    }

    private void registerRecipeViewerContent() {
        RecipeViewerBridge.category(new RecipeViewerCategory(id("ruby_workbench"),
                Component.translatable("recipe_viewer.nexuscore_example.ruby_workbench"),
                new ItemStack(ruby.get()), 132, 72, new ItemStack(rubyBlock.get())));

        RecipeViewerBridge.display(RecipeViewerDisplay.builder(id("ruby_workbench_preview"), id("ruby_workbench"), 132, 72)
                .page("infusion", page -> page
                        .text(Component.literal("Ruby Infusion"), 42, 4, 0x404040, false)
                        .itemInput(6, 20, new ItemStack(rawRuby.get()))
                        .fluidInput(28, 12, 12, 36, 4_000, new FluidStack(Fluids.WATER, 1_000))
                        .arrowProgress(52, 20, 2_000)
                        .itemOutput(92, 20, new ItemStack(ruby.get()))
                        .itemCatalyst(6, 50, new ItemStack(rubyBlock.get()))
                        .tooltip(52, 20, 24, 17, List.of(Component.literal("Animated work progress")))
                        .jeiTransferButton(98, 50)
                        .viewerControl("emi", "recipe_tree", Map.of("enabled", "true"))
                        .viewerControl("rei", "button", Map.of("x", "98", "y", "50", "width", "28", "height", "18", "text", "Info")))
                .page("charging", page -> page
                        .text(Component.literal("Charge Ruby Apple"), 34, 4, 0x404040, false)
                        .itemInput(12, 24, new ItemStack(ruby.get()))
                        .arrowProgress(52, 24, 1_200)
                        .itemOutput(92, 24, new ItemStack(rubyApple.get()))
                        .viewerControl("all", "tooltip", Map.of("x", "52", "y", "24", "width", "24", "height", "17",
                                "text", "This page is a second recipe-viewer display page.")))
                .build());
    }

    private static ResourceLocation id(String path) {
        return NexusIds.id(MOD_ID, path);
    }

    private static final class ExampleConfig extends NexusConfig {
        private final com.rollylindenshnizzer.nexuscore.config.IntOption machineEnergyCost;

        private ExampleConfig() {
            super(MOD_ID);
            machineEnergyCost = intOption("machine_energy_cost", 100).range(1, 10_000);
            machineEnergyCost
                    .group("machine")
                    .comment("Energy consumed by the example ruby press each operation.")
                    .translationKey("config.nexuscore_example.machine_energy_cost")
                    .serverSynced()
                    .requiresWorldReload();
        }
    }
}
