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

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "bootstrap", timeoutTicks = 100)
    public static void machineFrameworkProcessesHybridRecipes(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::machineFrameworkProcessesHybridRecipes);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "bootstrap", timeoutTicks = 100)
    public static void inventoryTransferRulesTraceRoutes(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::inventoryTransferRulesTraceRoutes);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "bootstrap", timeoutTicks = 100)
    public static void energyAndFluidTransfersRespectSides(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::energyAndFluidTransfersRespectSides);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "bootstrap", timeoutTicks = 100)
    public static void worldgenAndEntityDefinitionsGenerateDescriptors(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::worldgenAndEntityDefinitionsGenerateDescriptors);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "bootstrap", timeoutTicks = 100)
    public static void typedDataLoaderValidatesDatapackJson(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::typedDataLoaderValidatesDatapackJson);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "v13_dimension_portal", timeoutTicks = 100)
    public static void v13DimensionAndPortalDefinitionsValidate(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::v13DimensionAndPortalDefinitionsValidate);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "v13_structure_rules", timeoutTicks = 100)
    public static void v13StructureRulesExplainPlacement(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::v13StructureRulesExplainPlacement);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "v13_biome_ai", timeoutTicks = 100)
    public static void v13BiomeAndAiHelpersProduceDebugReports(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::v13BiomeAndAiHelpersProduceDebugReports);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "v13_automation", timeoutTicks = 100)
    public static void v13AutomationRoutesAndSimulatesNetworks(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::v13AutomationRoutesAndSimulatesNetworks);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "v13_data_balance_live", timeoutTicks = 100)
    public static void v13DataDefinitionsBalanceLiveReloadAndSafetyWorkTogether(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::v13DataDefinitionsBalanceLiveReloadAndSafetyWorkTogether);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "v13_recipe_compat", timeoutTicks = 100)
    public static void v13RecipeAndCompatibilityReportsAreConcrete(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::v13RecipeAndCompatibilityReportsAreConcrete);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "v13_runtime_registration", timeoutTicks = 100)
    public static void v13RuntimeInstallerMaterializesAdvancedSystems(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::v13RuntimeInstallerMaterializesAdvancedSystems);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "v13_runtime_registration", timeoutTicks = 100)
    public static void v13AdvancedCreationSystemsValidateAndGenerateData(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::v13AdvancedCreationSystemsValidateAndGenerateData);
    }

    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "v13_data_balance_live", timeoutTicks = 100)
    public static void v13AuthoringDataSimulationAndSafetyDescriptorsWork(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::v13AuthoringDataSimulationAndSafetyDescriptorsWork);
    }

    private static void run(GameTestHelper helper, Runnable scenario) {
        scenario.run();
        helper.succeed();
    }
}
