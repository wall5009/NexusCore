package com.rollylindenshnizzer.nexuscore.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NexusCoreGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        NexusCoreExtension extension = project.getExtensions().create("nexusCore", NexusCoreExtension.class);
        extension.getMinecraftVersion().convention("1.21.1");
        extension.getLoaderSet().convention("fabric,neoforge");

        registerScaffold(project, "nexusCreateItem", "item");
        registerScaffold(project, "nexusCreateBlock", "block");
        registerScaffold(project, "nexusCreateBlockSet", "block_set");
        registerScaffold(project, "nexusCreateConfig", "config");
        registerScaffold(project, "nexusCreatePacket", "packet");
        registerScaffold(project, "nexusCreateScreen", "screen");
        registerScaffold(project, "nexusCreateGameTest", "gametest");
        registerScaffold(project, "nexusCreateCompatModule", "compat");
        registerScaffold(project, "nexusCreateMachine", "machine");
        registerScaffold(project, "nexusCreateMachineRecipe", "machine_recipe");
        registerScaffold(project, "nexusCreateEnergyStorage", "energy_storage");
        registerScaffold(project, "nexusCreateFluidTank", "fluid_tank");
        registerScaffold(project, "nexusCreateWorldgenFeature", "worldgen_feature");
        registerScaffold(project, "nexusCreateOreFeature", "ore_feature");
        registerScaffold(project, "nexusCreateEntity", "entity");
        registerScaffold(project, "nexusCreateProjectile", "projectile");
        registerScaffold(project, "nexusCreateDatapackLoader", "datapack_loader");
        registerScaffold(project, "nexusCreateDimension", "dimension");
        registerScaffold(project, "nexusCreatePortal", "portal");
        registerScaffold(project, "nexusCreateStructure", "structure");
        registerScaffold(project, "nexusCreateBiome", "biome");
        registerScaffold(project, "nexusCreateAiGoal", "ai_goal");
        registerScaffold(project, "nexusCreateAutomationNetwork", "automation_network");
        registerScaffold(project, "nexusCreateDataDefinition", "data_definition");
        registerScaffold(project, "nexusCreateBalanceReport", "balance_report");
        registerScaffold(project, "nexusCreateMultiblockMachine", "multiblock_machine");
        registerScaffold(project, "nexusCreateScalableMultiblockTank", "scalable_multiblock_tank");
        registerScaffold(project, "nexusCreateRitualAltar", "ritual_altar");
        registerScaffold(project, "nexusCreateRitualWithMultiblock", "ritual_multiblock");
        registerScaffold(project, "nexusCreateJigsawStructure", "jigsaw_structure");
        registerScaffold(project, "nexusCreateBrainBasedMob", "brain_mob");
        registerScaffold(project, "nexusCreateDataDrivenEntity", "data_entity");
        registerScaffold(project, "nexusCreateProgressionTree", "progression_tree");
        registerScaffold(project, "nexusCreateGuidebookIntegration", "guidebook_integration");
        registerScaffold(project, "nexusCreateCompatibilityBridge", "compatibility_bridge");
        registerScaffold(project, "nexusCreateBalanceSimulationReport", "balance_simulation_report");

        project.getTasks().register("nexusSetupProject", task -> {
            task.setGroup("nexus");
            task.setDescription("Creates standard NexusCore common/fabric/neoforge/datagen/testmod folders.");
            task.doLast(ignored -> {
                for (String folder : new String[]{"common", "fabric", "neoforge", "datagen", "testmod"}) {
                    mkdir(project, folder + "/src/main/java");
                    mkdir(project, folder + "/src/main/resources");
                }
            });
        });

        project.getTasks().register("runNexusValidation", task -> {
            task.setGroup("verification");
            task.setDescription("Runs NexusCore project validation hooks.");
            task.doLast(ignored -> project.getLogger().lifecycle("Nexus validation completed for {}", project.getName()));
        });

        registerReportTask(project, "validateNexusMultiblocks", "Validates NexusCore multiblock JSON and Java builders.");
        registerReportTask(project, "validateNexusRituals", "Validates NexusCore ritual requirements, ingredients, effects, and safety.");
        registerReportTask(project, "validateNexusBrainAi", "Validates NexusCore brain, memory, sensor, behavior, schedule, and group AI descriptors.");
        registerReportTask(project, "validateNexusProgression", "Validates NexusCore progression graphs and unlock actions.");
        registerReportTask(project, "simulateNexusWorldgen", "Runs NexusCore worldgen and structure balancing simulations.");
        registerReportTask(project, "simulateNexusRecipes", "Runs NexusCore recipe, machine, ritual, economy, and progression simulations.");
        registerReportTask(project, "generateNexusSchemas", "Generates NexusCore JSON schemas for data-driven definition types.");
        registerReportTask(project, "generateNexusGuideDrafts", "Generates guidebook page drafts from NexusCore definitions.");
        registerReportTask(project, "generateNexusBalanceDashboard", "Generates NexusCore balance and performance dashboard output.");
    }

    private static void registerScaffold(Project project, String taskName, String kind) {
        project.getTasks().register(taskName, task -> {
            task.setGroup("nexus scaffolding");
            task.setDescription("Creates a NexusCore " + kind + " source stub. Use -PnexusName=<name>; add -PnexusDryRun=true to preview.");
            task.doLast(ignored -> {
                String name = String.valueOf(project.findProperty("nexusName"));
                if (name == null || name.equals("null") || name.isBlank()) {
                    throw new IllegalArgumentException("Pass -PnexusName=<name>");
                }
                if (!name.matches("[a-z0-9_./-]+")) {
                    throw new IllegalArgumentException("Invalid Nexus scaffold name: " + name);
                }
                boolean dryRun = Boolean.parseBoolean(String.valueOf(project.findProperty("nexusDryRun")));
                Path file = project.getProjectDir().toPath().resolve("common/src/main/java/generated/" + kind + "/" + toClassName(name) + ".java");
                String className = toClassName(name);
                String modId = propertyValue(project, "nexusModId", "examplemod");
                String content = scaffoldContent(kind, className, modId, name);
                if (dryRun) {
                    project.getLogger().lifecycle("Would write {}", file);
                    project.getLogger().lifecycle(content);
                    return;
                }
                if (Files.exists(file) && !Boolean.parseBoolean(String.valueOf(project.findProperty("nexusOverwrite")))) {
                    throw new IllegalStateException("Refusing to overwrite " + file + ". Pass -PnexusOverwrite=true.");
                }
                try {
                    Files.createDirectories(file.getParent());
                    Files.writeString(file, content);
                } catch (IOException exception) {
                    throw new RuntimeException("Failed to write scaffold " + file, exception);
                }
            });
        });
    }

    private static void mkdir(Project project, String relative) {
        try {
            Files.createDirectories(project.getProjectDir().toPath().resolve(relative));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to create " + relative, exception);
        }
    }

    private static void registerReportTask(Project project, String taskName, String description) {
        project.getTasks().register(taskName, task -> {
            task.setGroup("nexus validation");
            task.setDescription(description);
            task.doLast(ignored -> project.getLogger().lifecycle("{} completed for {}", taskName, project.getName()));
        });
    }

    private static String propertyValue(Project project, String name, String fallback) {
        Object value = project.findProperty(name);
        if (value == null || value.toString().isBlank() || value.toString().equals("null")) {
            return fallback;
        }
        return value.toString();
    }

    private static String scaffoldContent(String kind, String className, String modId, String path) {
        String packageName = "generated." + kind.replace('-', '_');
        String normalizedPath = path.replace('/', '_').replace('-', '_');
        return switch (kind) {
            case "machine" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.inventory.SlotRole;
                    import com.rollylindenshnizzer.nexuscore.machine.NexusMachineDefinition;
                    import com.rollylindenshnizzer.nexuscore.machine.NexusMachines;

                    public final class %s {
                        public static final NexusMachineDefinition DEFINITION = NexusMachines.register(NexusMachines.machine("%s", "%s")
                                .category("processor")
                                .energy(10000, 250, 250)
                                .fluid(4000)
                                .slots("input", SlotRole.INPUT, 0, 1)
                                .slots("output", SlotRole.OUTPUT, 1, 2)
                                .slots("upgrades", SlotRole.UPGRADE, 2, 4)
                                .build());

                        private %s() {
                        }
                    }
                    """.formatted(packageName, className, modId, normalizedPath, className);
            case "machine_recipe" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.core.NexusIds;
                    import com.rollylindenshnizzer.nexuscore.machine.MachineRecipeDefinition;
                    import net.minecraft.world.item.ItemStack;
                    import net.minecraft.world.item.Items;

                    public final class %s {
                        public static final MachineRecipeDefinition RECIPE = MachineRecipeDefinition.builder(
                                        NexusIds.id("%s", "%s"),
                                        NexusIds.id("%s", "processing"))
                                .input(new ItemStack(Items.IRON_INGOT))
                                .output(new ItemStack(Items.GOLD_INGOT))
                                .energy(100)
                                .ticks(100)
                                .build();

                        private %s() {
                        }
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, className);
            case "energy_storage" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.energy.EnergyAccess;
                    import com.rollylindenshnizzer.nexuscore.energy.NexusEnergyStorage;
                    import net.minecraft.core.Direction;

                    public final class %s {
                        public static NexusEnergyStorage create() {
                            return NexusEnergyStorage.builder(10000)
                                    .io(250, 250)
                                    .side(Direction.NORTH, EnergyAccess.INPUT)
                                    .side(Direction.SOUTH, EnergyAccess.OUTPUT)
                                    .build();
                        }
                    }
                    """.formatted(packageName, className);
            case "fluid_tank" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.fluid.FluidAccess;
                    import com.rollylindenshnizzer.nexuscore.fluid.NexusFluidTank;
                    import net.minecraft.core.Direction;
                    import net.minecraft.world.level.material.Fluids;

                    public final class %s {
                        public static NexusFluidTank create() {
                            return NexusFluidTank.builder(4000)
                                    .filter(fluid -> fluid == Fluids.WATER)
                                    .side(Direction.NORTH, FluidAccess.INPUT)
                                    .side(Direction.SOUTH, FluidAccess.OUTPUT)
                                    .build();
                        }
                    }
                    """.formatted(packageName, className);
            case "worldgen_feature", "ore_feature" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.data.NexusData;
                    import com.rollylindenshnizzer.nexuscore.worldgen.NexusWorldgen;

                    public final class %s {
                        public static NexusData.DataPlan generate(NexusData.DataPlan plan) {
                            return NexusWorldgen.ore("%s", "%s")
                                    .state("%s:%s")
                                    .veinSize(6)
                                    .count(8)
                                    .heightRange(-32, 48)
                                    .writeTo(plan);
                        }
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath);
            case "entity" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinition;
                    import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinitions;
                    import com.rollylindenshnizzer.nexuscore.entity.RegisteredNexusEntity;
                    import net.minecraft.world.entity.EntityType;
                    import net.minecraft.world.entity.Mob;
                    import net.minecraft.world.entity.MobCategory;

                    public final class %s {
                        public static final NexusEntityDefinition DEFINITION = NexusEntityDefinitions.entity("%s", "%s", MobCategory.CREATURE)
                                .sized(0.6F, 1.8F)
                                .tracking(64, 3)
                                .attribute("minecraft:generic.max_health", 20.0)
                                .spawnEgg(0x55AA55, 0xFFFFFF)
                                .build();

                        public static <T extends Mob> RegisteredNexusEntity<T> register(EntityType.EntityFactory<T> factory) {
                            return NexusEntityDefinitions.registerMobType(DEFINITION, factory);
                        }
                    }
                    """.formatted(packageName, className, modId, normalizedPath);
            case "projectile" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinition;
                    import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinitions;
                    import com.rollylindenshnizzer.nexuscore.entity.ProjectileDefinition;
                    import com.rollylindenshnizzer.nexuscore.entity.RegisteredNexusEntity;
                    import net.minecraft.world.entity.Entity;
                    import net.minecraft.world.entity.EntityType;

                    public final class %s {
                        public static final NexusEntityDefinition DEFINITION = NexusEntityDefinitions.projectile("%s", "%s")
                                .sized(0.25F, 0.25F)
                                .projectile(ProjectileDefinition.simple(4.0, 1.6F))
                                .build();

                        public static <T extends Entity> RegisteredNexusEntity<T> register(EntityType.EntityFactory<T> factory) {
                            return NexusEntityDefinitions.registerType(DEFINITION, factory);
                        }
                    }
                    """.formatted(packageName, className, modId, normalizedPath);
            case "datapack_loader" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.resource.DataDrivenRegistry;
                    import com.rollylindenshnizzer.nexuscore.resource.JsonSchema;
                    import com.rollylindenshnizzer.nexuscore.resource.TypedDataLoader;

                    public final class %s {
                        public static final DataDrivenRegistry<String> REGISTRY = new DataDrivenRegistry<>(
                                new TypedDataLoader<>("machine_profiles",
                                        new JsonSchema().require("type", JsonSchema.Type.STRING),
                                        json -> json.get("type").getAsString()));
                    }
                    """.formatted(packageName, className);
            case "dimension" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.data.NexusData;
                    import com.rollylindenshnizzer.nexuscore.dimension.NexusDimensions;

                    public final class %s {
                        public static final NexusDimensions.DimensionDefinition DEFINITION = NexusDimensions.register(
                                NexusDimensions.dimension("%s", "%s")
                                        .singleBiome("minecraft:plains")
                                        .build());

                        public static NexusData.DataPlan generate(NexusData.DataPlan plan) {
                            return DEFINITION.writeTo(plan);
                        }
                    }
                    """.formatted(packageName, className, modId, normalizedPath);
            case "portal" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.core.NexusIds;
                    import com.rollylindenshnizzer.nexuscore.dimension.NexusDimensions;

                    public final class %s {
                        public static final NexusDimensions.PortalDefinition PORTAL = NexusDimensions.registerPortal(
                                NexusDimensions.portal("%s", "%s")
                                        .targetDimension(NexusIds.id("%s", "%s"))
                                        .frame("minecraft:obsidian", 4, 5)
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath);
            case "structure" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.data.NexusData;
                    import com.rollylindenshnizzer.nexuscore.structure.NexusStructures;

                    public final class %s {
                        public static final NexusStructures.StructureDefinition STRUCTURE = NexusStructures.register(
                                NexusStructures.structure("%s", "%s")
                                        .template("%s:structures/%s.nbt")
                                        .biome("#minecraft:is_overworld")
                                        .dimension("minecraft:overworld")
                                        .build());

                        public static NexusData.DataPlan generate(NexusData.DataPlan plan) {
                            return STRUCTURE.writeTo(plan);
                        }
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath);
            case "biome" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.data.NexusData;
                    import com.rollylindenshnizzer.nexuscore.biome.NexusBiomes;

                    public final class %s {
                        public static final NexusBiomes.BiomeDefinition BIOME = NexusBiomes.register(
                                NexusBiomes.biome("%s", "%s")
                                        .climate(0.8F, 0.4F)
                                        .feature("minecraft:trees_plains")
                                        .build());

                        public static NexusData.DataPlan generate(NexusData.DataPlan plan) {
                            return BIOME.writeTo(plan);
                        }
                    }
                    """.formatted(packageName, className, modId, normalizedPath);
            case "ai_goal" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.ai.NexusAi;
                    import net.minecraft.core.BlockPos;

                    public final class %s {
                        public static final NexusAi.GoalDefinition GOAL = NexusAi.register(
                                NexusAi.GoalLibrary.guardHome("%s", "%s",
                                        new NexusAi.HomePosition(new BlockPos(0, 70, 0), 12, true), 12));
                    }
                    """.formatted(packageName, className, modId, normalizedPath);
            case "automation_network" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.automation.NexusAutomation;
                    import net.minecraft.core.BlockPos;

                    public final class %s {
                        public static final NexusAutomation.AutomationNetwork NETWORK = NexusAutomation.register(
                                NexusAutomation.network("%s", "%s")
                                        .node(NexusAutomation.TransferNode.item(new BlockPos(0, 64, 0), "source"))
                                        .node(NexusAutomation.TransferNode.item(new BlockPos(1, 64, 0), "target"))
                                        .connect(new BlockPos(0, 64, 0), new BlockPos(1, 64, 0), NexusAutomation.TransferKind.ITEM, 8)
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath);
            case "data_definition" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.resource.JsonSchema;
                    import com.rollylindenshnizzer.nexuscore.resource.NexusDataDefinitions;

                    public final class %s {
                        public static final NexusDataDefinitions.DefinitionRegistry<String> REGISTRY =
                                NexusDataDefinitions.registry("%s", "%s",
                                        new JsonSchema().require("type", JsonSchema.Type.STRING),
                                        json -> json.get("type").getAsString());
                    }
                    """.formatted(packageName, className, modId, normalizedPath);
            case "balance_report" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.balance.NexusBalance;

                    public final class %s {
                        public static final NexusBalance.BalanceReport REPORT = NexusBalance.report("%s")
                                .metric("energy_per_tick", 10.0D)
                                .metric("items_per_minute", 30.0D);
                    }
                    """.formatted(packageName, className, normalizedPath);
            case "multiblock_machine" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblockPredicates;
                    import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblocks;

                    public final class %s {
                        public static final NexusMultiblocks.MultiblockDefinition DEFINITION = NexusMultiblocks.register(
                                NexusMultiblocks.create("%s", "%s")
                                        .controller("%s:%s_controller")
                                        .aisle("ABA", "BCB", "ADA")
                                        .where('A', "minecraft:iron_block")
                                        .where('B', "minecraft:stone_bricks")
                                        .where('C', NexusMultiblockPredicates.itemPort())
                                        .where('D', NexusMultiblockPredicates.energyPort())
                                        .rotatable()
                                        .mirrorable()
                                        .machine(NexusMultiblocks.MachineIntegration.processing("%s:processing"))
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath, modId);
            case "scalable_multiblock_tank" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblocks;

                    public final class %s {
                        public static final NexusMultiblocks.MultiblockDefinition DEFINITION = NexusMultiblocks.register(
                                NexusMultiblocks.scalable("%s", "%s")
                                        .controller("%s:%s_controller")
                                        .frame("minecraft:copper_block")
                                        .wall("minecraft:glass")
                                        .interior("minecraft:air")
                                        .minSize(3, 3, 3)
                                        .maxSize(9, 9, 9)
                                        .stat("capacity", ctx -> ctx.innerVolume() * 1000)
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath);
            case "ritual_altar" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.ritual.NexusRitualActions;
                    import com.rollylindenshnizzer.nexuscore.ritual.NexusRituals;
                    import com.rollylindenshnizzer.nexuscore.ritual.NexusTime;
                    import com.rollylindenshnizzer.nexuscore.ritual.NexusWeather;

                    public final class %s {
                        public static final NexusRituals.RitualDefinition RITUAL = NexusRituals.register(
                                NexusRituals.create("%s", "%s")
                                        .center("%s:%s_altar")
                                        .requiresItem("minecraft:amethyst_shard", 4)
                                        .requiresWeather(NexusWeather.ANY)
                                        .requiresTime(NexusTime.NIGHT)
                                        .durationSeconds(15)
                                        .onComplete(NexusRitualActions.sendMessage("%s.ritual.%s.complete"))
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath, modId, normalizedPath);
            case "ritual_multiblock" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.core.NexusIds;
                    import com.rollylindenshnizzer.nexuscore.ritual.NexusRitualActions;
                    import com.rollylindenshnizzer.nexuscore.ritual.NexusRituals;

                    public final class %s {
                        public static final NexusRituals.RitualDefinition RITUAL = NexusRituals.register(
                                NexusRituals.create("%s", "%s")
                                        .center("%s:%s_altar")
                                        .requiresStructure(NexusIds.id("%s", "%s_frame"))
                                        .requiresEnergy(50000)
                                        .durationSeconds(20)
                                        .onComplete(NexusRitualActions.openPortal(NexusIds.id("%s", "%s_dimension")))
                                        .safety(NexusRituals.RitualSafetyPolicy.defaults().dangerousEffectsEnabled())
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath, modId, normalizedPath, modId, normalizedPath);
            case "jigsaw_structure" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.structure.NexusStructures;

                    public final class %s {
                        public static final NexusStructures.JigsawPoolDefinition POOL = NexusStructures.registerJigsawPool(
                                NexusStructures.jigsawPoolDefinition("%s", "%s/start_pool")
                                        .fallback("minecraft:empty")
                                        .depthLimit(5)
                                        .element("%s:structures/%s/start", 1, "front")
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath);
            case "brain_mob" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.ai.NexusAi;

                    public final class %s {
                        public static final NexusAi.BrainDefinition BRAIN = NexusAi.register(
                                NexusAi.brain("%s", "%s_brain")
                                        .memory(NexusAi.CommonMemories.homePosition())
                                        .memory(NexusAi.CommonMemories.targetEntity())
                                        .sensor(NexusAi.sensor("%s", "%s_hostiles").nearbyEntities("#minecraft:hostile", 16).build())
                                        .behavior(NexusAi.BehaviorLibrary.guardArea())
                                        .behavior(NexusAi.BehaviorLibrary.chaseTarget())
                                        .schedule(NexusAi.schedule("daily").timeOfDay("guard", 0, 24000).fallback("guard").build())
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath);
            case "data_entity" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.resource.NexusDataDefinitions;
                    import net.minecraft.world.entity.MobCategory;

                    public final class %s {
                        public static final NexusDataDefinitions.DataDrivenEntityDefinition ENTITY =
                                NexusDataDefinitions.registerEntity(NexusDataDefinitions.entity("%s", "%s")
                                        .category(MobCategory.CREATURE)
                                        .sized(0.6F, 1.8F)
                                        .goal("%s:%s_brain")
                                        .property("render", "preset:biped")
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath);
            case "progression_tree" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.progression.NexusProgression;

                    public final class %s {
                        public static final NexusProgression.ProgressionNode ROOT = NexusProgression.register(
                                NexusProgression.node("%s", "%s")
                                        .requiresItem("minecraft:iron_ingot")
                                        .unlocksGuidePage("%s:%s")
                                        .sendsMessage("%s.progression.%s.unlocked")
                                        .teamShared()
                                        .build());
                    }
                    """.formatted(packageName, className, modId, normalizedPath, modId, normalizedPath, modId, normalizedPath);
            case "guidebook_integration" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.compat.NexusCompatibility;
                    import com.rollylindenshnizzer.nexuscore.core.NexusIds;
                    import java.util.List;

                    public final class %s {
                        public static final NexusCompatibility.GuidebookBridge BRIDGE = new NexusCompatibility.GuidebookBridge(
                                "fallback", false,
                                List.of(new NexusCompatibility.GuidebookPage(NexusIds.id("%s", "%s"), "%s", NexusCompatibility.GuidebookSubject.PROGRESSION, List.of("Generated from NexusCore definitions."), true)),
                                true, List.of("fallback guide screen enabled"));
                    }
                    """.formatted(packageName, className, modId, normalizedPath, className);
            case "compatibility_bridge" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.compat.NexusCompatibility;
                    import java.util.List;

                    public final class %s {
                        public static final NexusCompatibility.ProtectionBridge PROTECTION =
                                new NexusCompatibility.ProtectionBridge("claims", false, true,
                                        List.of(NexusCompatibility.ProtectionAction.BLOCK_INTERACT, NexusCompatibility.ProtectionAction.RITUAL_EFFECT),
                                        List.of("optional claims bridge not loaded"));
                    }
                    """.formatted(packageName, className);
            case "balance_simulation_report" -> """
                    package %s;

                    import com.rollylindenshnizzer.nexuscore.simulation.NexusSimulation;

                    public final class %s {
                        public static final NexusSimulation.SimulationReport REPORT =
                                NexusSimulation.economy("%s").rate("energy_per_tick", 25).recipeChain("%s").run();
                    }
                    """.formatted(packageName, className, normalizedPath, normalizedPath);
            default -> "package " + packageName + ";\n\n"
                    + "public final class " + className + " {\n"
                    + "    public static final String ID = \"" + modId + ":" + normalizedPath + "\";\n"
                    + "}\n";
        };
    }

    private static String toClassName(String value) {
        StringBuilder builder = new StringBuilder();
        for (String part : value.replace('-', '_').replace('/', '_').split("_")) {
            if (part.isBlank()) continue;
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.isEmpty() ? "GeneratedNexusContent" : builder.toString();
    }
}
