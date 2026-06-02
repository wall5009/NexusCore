package com.rollylindenshnizzer.nexuscore.testmod.target.fabric.mc1201;

import com.rollylindenshnizzer.nexuscore.adapter.fabric.mc1201.Fabric1201ServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.testmod.NexusCoreTestMod;

public final class NexusCoreTestModFabric1201 {
    public void onInitialize() {
        NexusServices.install(new Fabric1201ServiceProvider());
        NexusCoreTestMod.init();
        NexusCoreCommonBootstrap.init();
    }
}
