package com.rollylindenshnizzer.nexuscore.testmod.target.quilt.mc1211;

import com.rollylindenshnizzer.nexuscore.adapter.quilt.mc1211.Quilt1211ServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.testmod.NexusCoreTestMod;

public final class NexusCoreTestModQuilt1211 {
    public void onInitialize() {
        NexusServices.install(new Quilt1211ServiceProvider());
        NexusCoreTestMod.init();
        NexusCoreCommonBootstrap.init();
    }
}
