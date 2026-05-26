package com.rollylindenshnizzer.nexuscore.registry;

import com.rollylindenshnizzer.nexuscore.core.NexusDiagnostics;

public final class NexusRegistryReports {
    public static NexusDiagnostics.Report report(NexusRegistryGroup group) {
        NexusDiagnostics.Report report = NexusDiagnostics.report("registry", group.modId());
        report.info("items", Integer.toString(count(group.items())));
        report.info("blocks", Integer.toString(count(group.blocks())));
        report.info("block_entities", Integer.toString(count(group.blockEntities())));
        report.info("menus", Integer.toString(count(group.menus())));
        report.info("entities", Integer.toString(count(group.entityTypes())));
        report.info("sounds", Integer.toString(count(group.sounds())));
        report.info("particles", Integer.toString(count(group.particles())));
        report.info("creative_tabs", Integer.toString(count(group.creativeTabs())));
        report.info("recipe_types", Integer.toString(count(group.recipeTypes())));
        report.info("recipe_serializers", Integer.toString(count(group.recipeSerializers())));
        return report;
    }

    private static int count(Iterable<?> iterable) {
        int count = 0;
        for (Object ignored : iterable) {
            count++;
        }
        return count;
    }

    private NexusRegistryReports() {
    }
}
