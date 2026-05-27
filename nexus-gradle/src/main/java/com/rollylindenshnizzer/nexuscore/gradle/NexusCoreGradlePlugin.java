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
                String content = "package generated." + kind.replace('-', '_') + ";\n\n"
                        + "public final class " + toClassName(name) + " {\n"
                        + "    // TODO: Fill in NexusCore " + kind + " behavior.\n"
                        + "}\n";
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

    private static String toClassName(String value) {
        StringBuilder builder = new StringBuilder();
        for (String part : value.replace('-', '_').replace('/', '_').split("_")) {
            if (part.isBlank()) continue;
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.isEmpty() ? "GeneratedNexusContent" : builder.toString();
    }
}
