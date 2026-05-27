package com.rollylindenshnizzer.nexuscore.neoforge.test;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.test.NexusCoreGameTestScenarios;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NexusCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NexusCoreNeoForgeGameTests {
    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "bootstrap", timeoutTicks = 100)
    public static void energyStorageRateLimits(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::energyStorageRateLimits);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "config_schema", timeoutTicks = 100)
    public static void configSchemaExportsSyncedOptions(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::configSchemaExportsSyncedOptions);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "datagen_validation", timeoutTicks = 100)
    public static void datagenValidationCatchesErrorsAndAcceptsValidPlans(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::datagenValidationCatchesErrorsAndAcceptsValidPlans);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "packet_diagnostics", timeoutTicks = 100)
    public static void packetDiagnosticsCompareVersions(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::packetDiagnosticsCompareVersions);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "recipe_viewer_controls", timeoutTicks = 100)
    public static void recipeViewerControlsHavePortableFallbacks(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::recipeViewerControlsHavePortableFallbacks);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "performance_benchmarks", timeoutTicks = 100)
    public static void benchmarkSuiteRecordsProfilerSamples(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::benchmarkSuiteRecordsProfilerSamples);
    }

    private static void run(GameTestHelper helper, Runnable scenario) {
        scenario.run();
        helper.succeed();
    }
}
