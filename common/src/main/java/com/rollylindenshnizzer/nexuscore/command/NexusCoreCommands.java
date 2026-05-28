package com.rollylindenshnizzer.nexuscore.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.ai.NexusAi;
import com.rollylindenshnizzer.nexuscore.automation.NexusAutomation;
import com.rollylindenshnizzer.nexuscore.authoring.NexusAuthoring;
import com.rollylindenshnizzer.nexuscore.balance.NexusBalance;
import com.rollylindenshnizzer.nexuscore.biome.NexusBiomes;
import com.rollylindenshnizzer.nexuscore.component.ComponentDebug;
import com.rollylindenshnizzer.nexuscore.compat.NexusCompatibility;
import com.rollylindenshnizzer.nexuscore.config.ConfigSchemaExporter;
import com.rollylindenshnizzer.nexuscore.config.NexusConfigRegistry;
import com.rollylindenshnizzer.nexuscore.core.NexusModuleRegistry;
import com.rollylindenshnizzer.nexuscore.core.NexusVersion;
import com.rollylindenshnizzer.nexuscore.data.DatagenReportWriters;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.data.NexusDataValidator;
import com.rollylindenshnizzer.nexuscore.debug.NexusDebugWorkbench;
import com.rollylindenshnizzer.nexuscore.debug.NexusDoctor;
import com.rollylindenshnizzer.nexuscore.dimension.NexusDimensions;
import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinitions;
import com.rollylindenshnizzer.nexuscore.live.NexusLiveReload;
import com.rollylindenshnizzer.nexuscore.machine.NexusMachines;
import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblocks;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import com.rollylindenshnizzer.nexuscore.progression.NexusProgression;
import com.rollylindenshnizzer.nexuscore.recipe.NexusRecipeFamilies;
import com.rollylindenshnizzer.nexuscore.registry.NexusContentManifest;
import com.rollylindenshnizzer.nexuscore.resource.NexusDataDefinitions;
import com.rollylindenshnizzer.nexuscore.ritual.NexusRituals;
import com.rollylindenshnizzer.nexuscore.runtime.NexusRuntimeContent;
import com.rollylindenshnizzer.nexuscore.simulation.NexusSimulation;
import com.rollylindenshnizzer.nexuscore.structure.NexusStructures;
import com.rollylindenshnizzer.nexuscore.worldgen.NexusWorldgen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class NexusCoreCommands {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean installed;

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        NexusCommands.register(root());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("nexus")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("version").executes(context -> {
                    context.getSource().sendSystemMessage(Component.literal("NexusCore " + NexusVersion.VERSION
                            + " | MC " + NexusVersion.MINECRAFT_VERSION
                            + " | Java " + NexusVersion.JAVA_VERSION
                            + " | Architectury " + NexusVersion.ARCHITECTURY_VERSION));
                    NexusModuleRegistry.modules().forEach(module -> context.getSource().sendSystemMessage(
                            Component.literal(module.name() + " " + module.version() + " [" + module.stabilityLevel() + "]")));
                    return 1;
                }))
                .then(Commands.literal("doctor").executes(context -> {
                    var report = NexusDoctor.run(NexusCore.MOD_ID);
                    context.getSource().sendSystemMessage(Component.literal(report.summary()));
                    report.issues().stream().limit(8).forEach(issue -> context.getSource().sendSystemMessage(
                            Component.literal(issue.severity() + " " + issue.code() + ": " + issue.title() + " - " + issue.detail())));
                    return report.hasErrors() ? 0 : 1;
                }))
                .then(Commands.literal("dump")
                        .then(Commands.literal("registries").executes(context -> send(context.getSource(),
                                GSON.toJson(NexusModuleRegistry.toJson()))))
                        .then(Commands.literal("content").executes(context -> send(context.getSource(),
                                GSON.toJson(NexusContentManifest.json(NexusCore.MOD_ID)))))
                        .then(Commands.literal("machines").executes(context -> send(context.getSource(), NexusMachines.debugSummary())))
                        .then(Commands.literal("worldgen").executes(context -> send(context.getSource(), NexusWorldgen.debugSummary())))
                        .then(Commands.literal("entities").executes(context -> send(context.getSource(), NexusEntityDefinitions.debugSummary())))
                        .then(Commands.literal("dimensions").executes(context -> send(context.getSource(), NexusDimensions.debugSummary())))
                        .then(Commands.literal("structures").executes(context -> send(context.getSource(), NexusStructures.debugSummary())))
                        .then(Commands.literal("biomes").executes(context -> send(context.getSource(), NexusBiomes.debugSummary())))
                        .then(Commands.literal("ai").executes(context -> send(context.getSource(), NexusAi.debugSummary())))
                        .then(Commands.literal("automation").executes(context -> send(context.getSource(), NexusAutomation.debugSummary())))
                        .then(Commands.literal("multiblocks").executes(context -> send(context.getSource(), NexusMultiblocks.debugSummary())))
                        .then(Commands.literal("rituals").executes(context -> send(context.getSource(), NexusRituals.debugSummary())))
                        .then(Commands.literal("progression").executes(context -> send(context.getSource(), NexusProgression.debugSummary())))
                        .then(Commands.literal("authoring").executes(context -> send(context.getSource(), NexusAuthoring.debugSummary())))
                        .then(Commands.literal("simulation").executes(context -> send(context.getSource(), NexusSimulation.debugSummary())))
                        .then(Commands.literal("data-definitions").executes(context -> send(context.getSource(), NexusDataDefinitions.debugSummary())))
                        .then(Commands.literal("recipes-2").executes(context -> send(context.getSource(), "recipeFamilies="
                                + NexusRecipeFamilies.families().size() + ", warnings=" + NexusRecipeFamilies.balanceReport().warnings().size())))
                        .then(Commands.literal("compat").executes(context -> send(context.getSource(), NexusCompatibility.debugSummary())))
                        .then(Commands.literal("debug-workbench").executes(context -> send(context.getSource(), NexusDebugWorkbench.debugSummary())))
                        .then(Commands.literal("runtime-content").executes(context -> send(context.getSource(), NexusRuntimeContent.debugSummary())))
                        .then(Commands.literal("live-reload").executes(context -> send(context.getSource(), NexusLiveReload.debugSummary())))
                        .then(Commands.literal("config-schema").executes(context -> {
                            NexusConfigRegistry.configs().forEach(config -> context.getSource().sendSystemMessage(Component.literal(
                                    GSON.toJson(ConfigSchemaExporter.jsonSchema(config, NexusVersion.VERSION)))));
                            return 1;
                        }))
                        .then(Commands.literal("packets").executes(context -> send(context.getSource(),
                                NexusNetworking.diagnostics().toString()))))
                .then(Commands.literal("validate")
                        .then(Commands.literal("assets").executes(context -> validateData(context.getSource())))
                        .then(Commands.literal("tags").executes(context -> validateData(context.getSource())))
                        .then(Commands.literal("recipes").executes(context -> validateData(context.getSource())))
                        .then(Commands.literal("machines").executes(context -> send(context.getSource(), NexusMachines.debugSummary())))
                        .then(Commands.literal("worldgen").executes(context -> send(context.getSource(), NexusWorldgen.debugSummary())))
                        .then(Commands.literal("entities").executes(context -> send(context.getSource(), NexusEntityDefinitions.debugSummary())))
                        .then(Commands.literal("dimensions").executes(context -> send(context.getSource(), NexusDimensions.validate().summary())))
                        .then(Commands.literal("structures").executes(context -> send(context.getSource(), NexusStructures.validate().summary())))
                        .then(Commands.literal("ai").executes(context -> send(context.getSource(), NexusAi.debugReport().healthy()
                                ? "AI validation passed" : NexusAi.debugReport().warnings().toString())))
                        .then(Commands.literal("automation").executes(context -> send(context.getSource(), NexusAutomation.validate().passed()
                                ? "Automation validation passed" : NexusAutomation.validate().errors().toString())))
                        .then(Commands.literal("multiblocks").executes(context -> send(context.getSource(), NexusMultiblocks.validate().summary())))
                        .then(Commands.literal("rituals").executes(context -> send(context.getSource(), NexusRituals.validate().summary())))
                        .then(Commands.literal("progression").executes(context -> send(context.getSource(), NexusProgression.validate().summary())))
                        .then(Commands.literal("data-definitions").executes(context -> send(context.getSource(), NexusDataDefinitions.validateAll().passed()
                                ? "Data definition validation passed" : NexusDataDefinitions.validateAll().errors().toString())))
                        .then(Commands.literal("balance").executes(context -> send(context.getSource(), NexusBalance.report("runtime").summary()))))
                .then(Commands.literal("inspect")
                        .then(Commands.literal("held-item").executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            context.getSource().sendSystemMessage(Component.literal(ComponentDebug.prettyPrint(player.getMainHandItem())));
                            return 1;
                        }))
                        .then(Commands.literal("machine").executes(context -> send(context.getSource(), NexusMachines.debugSummary())))
                        .then(Commands.literal("inventory").executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            context.getSource().sendSystemMessage(Component.literal("player inventory slots=" + player.getInventory().getContainerSize()));
                            return 1;
                        }))
                        .then(Commands.literal("energy").executes(context -> send(context.getSource(), "Energy systems are exposed through NexusEnergyStorage debug sections.")))
                        .then(Commands.literal("fluid").executes(context -> send(context.getSource(), "Fluid systems are exposed through NexusFluidTank debug sections.")))
                        .then(Commands.literal("dimension").executes(context -> send(context.getSource(), NexusDimensions.debugSummary())))
                        .then(Commands.literal("structure").executes(context -> send(context.getSource(), NexusStructures.debugSummary())))
                        .then(Commands.literal("ai").executes(context -> send(context.getSource(), NexusAi.debugSummary())))
                        .then(Commands.literal("automation").executes(context -> send(context.getSource(), NexusAutomation.debugSummary())))
                        .then(Commands.literal("multiblock").executes(context -> send(context.getSource(), NexusMultiblocks.debugSummary())))
                        .then(Commands.literal("ritual").executes(context -> send(context.getSource(), NexusRituals.debugSummary())))
                        .then(Commands.literal("progression").executes(context -> send(context.getSource(), NexusProgression.debugSummary())))
                        .then(Commands.literal("data-definition").executes(context -> send(context.getSource(), NexusDataDefinitions.debugSummary())))
                        .then(Commands.literal("menu").executes(context -> send(context.getSource(), "Open menu inspection uses MenuDebugInfo snapshots.")))
                        .then(Commands.literal("target-block").executes(context -> {
                            context.getSource().sendSystemMessage(Component.literal("Target block inspection is available in the debug screen."));
                            return 1;
                        }))
                        .then(Commands.literal("target-entity").executes(context -> {
                            context.getSource().sendSystemMessage(Component.literal("Target entity inspection is available in the debug screen."));
                            return 1;
                        })))
                .then(Commands.literal("simulate")
                        .then(Commands.literal("automation").executes(context -> send(context.getSource(), NexusAutomation.debugSummary())))
                        .then(Commands.literal("structure").executes(context -> send(context.getSource(),
                                NexusStructures.simulate("runtime", 25, NexusStructures.structures()).summary())))
                        .then(Commands.literal("worldgen").executes(context -> send(context.getSource(),
                                NexusSimulation.worldgen("runtime_worldgen").seeds(25).dimension("minecraft:overworld").run().metrics().toString())))
                        .then(Commands.literal("balance").executes(context -> send(context.getSource(),
                                NexusBalance.scenario("runtime").parameter("items", 64).ticks(200).run().metrics().toString()))))
                .then(Commands.literal("multiblock")
                        .then(Commands.literal("validate").executes(context -> send(context.getSource(), NexusMultiblocks.validate().summary())))
                        .then(Commands.literal("export").executes(context -> send(context.getSource(),
                                NexusMultiblocks.definitions().stream().map(definition -> definition.id() + " -> " + definition.javaBuilderSample()).toList().toString())))
                        .then(Commands.literal("preview").executes(context -> send(context.getSource(),
                                NexusMultiblocks.definitions().stream().map(definition -> definition.id() + " layers=" + definition.previewLayers().size()).toList().toString()))))
                .then(Commands.literal("ritual")
                        .then(Commands.literal("list").executes(context -> send(context.getSource(), NexusRituals.runtime().active().toString())))
                        .then(Commands.literal("validate").executes(context -> send(context.getSource(), NexusRituals.validate().summary())))
                        .then(Commands.literal("start").executes(context -> send(context.getSource(), "Use the Java runtime API to provide a RitualContext; command wiring is present.")))
                        .then(Commands.literal("cancel").executes(context -> send(context.getSource(), "Use the Java runtime API with an active ritual UUID; command wiring is present."))))
                .then(Commands.literal("brain")
                        .then(Commands.literal("dump").executes(context -> send(context.getSource(),
                                NexusAi.brains().stream().map(NexusAi.BrainDefinition::explain).toList().toString()))))
                .then(Commands.literal("structure")
                        .then(Commands.literal("simulate").executes(context -> send(context.getSource(),
                                NexusStructures.simulate("command", 50, NexusStructures.structures()).summary()))))
                .then(Commands.literal("progression")
                        .then(Commands.literal("inspect").executes(context -> send(context.getSource(),
                                NexusProgression.runtime().inspect(context.getSource().getTextName()).toString()))))
                .then(Commands.literal("balance")
                        .then(Commands.literal("dashboard").executes(context -> send(context.getSource(),
                                NexusSimulation.dashboard("runtime", List.of(NexusBalance.report("runtime")), List.of()).toMarkdown()))))
                .then(Commands.literal("compat")
                        .then(Commands.literal("report").executes(context -> send(context.getSource(),
                                NexusCompatibility.matrix().dashboard().summary()))));
    }

    private static int validateData(CommandSourceStack source) {
        var report = NexusDataValidator.validatePlan(NexusData.plan(NexusCore.MOD_ID));
        source.sendSystemMessage(Component.literal(report.summary()));
        source.sendSystemMessage(Component.literal(DatagenReportWriters.toMarkdown(report)));
        return report.hasErrors() ? 0 : 1;
    }

    private static int send(CommandSourceStack source, String value) {
        source.sendSystemMessage(Component.literal(value));
        return 1;
    }

    private NexusCoreCommands() {
    }
}
