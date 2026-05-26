package com.rollylindenshnizzer.nexuscore.example.fabric;

import com.rollylindenshnizzer.nexuscore.data.NexusDataProvider;
import com.rollylindenshnizzer.nexuscore.example.NexusCoreExampleContent;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class NexusCoreExampleFabricDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider((FabricDataGenerator.Pack.Factory<NexusDataProvider>) output ->
                new NexusDataProvider(output, NexusCoreExampleContent.MOD_ID, NexusCoreExampleContent.populateGeneratedData()));
    }
}
