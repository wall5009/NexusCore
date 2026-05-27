package com.rollylindenshnizzer.nexuscore.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.component.ComponentDebug;
import com.rollylindenshnizzer.nexuscore.config.ConfigSchemaExporter;
import com.rollylindenshnizzer.nexuscore.config.NexusConfigRegistry;
import com.rollylindenshnizzer.nexuscore.core.NexusModuleRegistry;
import com.rollylindenshnizzer.nexuscore.core.NexusVersion;
import com.rollylindenshnizzer.nexuscore.data.DatagenReportWriters;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.data.NexusDataValidator;
import com.rollylindenshnizzer.nexuscore.debug.NexusDoctor;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import com.rollylindenshnizzer.nexuscore.registry.NexusContentManifest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

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
                        .then(Commands.literal("recipes").executes(context -> validateData(context.getSource()))))
                .then(Commands.literal("inspect")
                        .then(Commands.literal("held-item").executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            context.getSource().sendSystemMessage(Component.literal(ComponentDebug.prettyPrint(player.getMainHandItem())));
                            return 1;
                        }))
                        .then(Commands.literal("target-block").executes(context -> {
                            context.getSource().sendSystemMessage(Component.literal("Target block inspection is available in the debug screen."));
                            return 1;
                        }))
                        .then(Commands.literal("target-entity").executes(context -> {
                            context.getSource().sendSystemMessage(Component.literal("Target entity inspection is available in the debug screen."));
                            return 1;
                        })));
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
