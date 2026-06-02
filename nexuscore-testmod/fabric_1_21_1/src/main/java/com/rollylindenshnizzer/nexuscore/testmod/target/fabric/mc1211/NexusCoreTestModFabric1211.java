package com.rollylindenshnizzer.nexuscore.testmod.target.fabric.mc1211;

import com.rollylindenshnizzer.nexuscore.adapter.fabric.mc1211.Fabric1211ServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.testmod.NexusCoreTestMod;

public final class NexusCoreTestModFabric1211 {
    public void onInitialize() {
        NexusServices.install(new Fabric1211ServiceProvider());
        NexusCoreTestMod.init();
        NexusCoreCommonBootstrap.init();
    }
}
