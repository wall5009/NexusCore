package com.rollylindenshnizzer.nexuscore.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NexusCoreGradlePluginFunctionalTest {
    @TempDir
    Path projectDir;

    @Test
    void pluginRegistersValidationAndScaffoldingTasks() throws IOException {
        writeBuildFile();

        BuildResult result = gradle("tasks", "--all").build();

        assertTrue(result.getOutput().contains("runNexusValidation"));
        assertTrue(result.getOutput().contains("nexusCreateItem"));
        assertTrue(result.getOutput().contains("nexusSetupProject"));
    }

    @Test
    void setupProjectCreatesDualLoaderLayout() throws IOException {
        writeBuildFile();

        BuildResult result = gradle("nexusSetupProject").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":nexusSetupProject").getOutcome());
        assertTrue(Files.isDirectory(projectDir.resolve("common/src/main/java")));
        assertTrue(Files.isDirectory(projectDir.resolve("fabric/src/main/resources")));
        assertTrue(Files.isDirectory(projectDir.resolve("neoforge/src/main/java")));
        assertTrue(Files.isDirectory(projectDir.resolve("datagen/src/main/resources")));
        assertTrue(Files.isDirectory(projectDir.resolve("testmod/src/main/java")));
    }

    @Test
    void scaffoldTaskWritesExpectedSourceFile() throws IOException {
        writeBuildFile();

        BuildResult result = gradle("nexusCreateItem", "-PnexusName=ruby_item").build();

        Path generated = projectDir.resolve("common/src/main/java/generated/item/RubyItem.java");
        assertEquals(TaskOutcome.SUCCESS, result.task(":nexusCreateItem").getOutcome());
        assertTrue(Files.exists(generated));
        assertTrue(Files.readString(generated).contains("public final class RubyItem"));
    }

    @Test
    void dryRunScaffoldDoesNotWriteSourceFile() throws IOException {
        writeBuildFile();

        BuildResult result = gradle("nexusCreateBlock", "-PnexusName=ruby_block", "-PnexusDryRun=true").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":nexusCreateBlock").getOutcome());
        assertFalse(Files.exists(projectDir.resolve("common/src/main/java/generated/block/RubyBlock.java")));
        assertTrue(result.getOutput().contains("Would write"));
    }

    private GradleRunner gradle(String... arguments) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments(arguments)
                .withPluginClasspath();
    }

    private void writeBuildFile() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'nexuscore-plugin-test'\n");
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'com.rollylindenshnizzer.nexuscore'
                }

                nexusCore {
                    modId = 'testmod'
                    minecraftVersion = '1.21.1'
                    loaderSet = 'fabric,neoforge'
                }
                """);
    }
}
