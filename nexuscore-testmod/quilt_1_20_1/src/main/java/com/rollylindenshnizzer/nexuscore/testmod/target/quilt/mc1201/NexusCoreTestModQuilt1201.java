package com.rollylindenshnizzer.nexuscore.testmod.target.quilt.mc1201;

import com.rollylindenshnizzer.nexuscore.adapter.quilt.mc1201.Quilt1201ServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.testmod.NexusCoreTestMod;

public final class NexusCoreTestModQuilt1201 {
    public void onInitialize() {
        NexusServices.install(new Quilt1201ServiceProvider());
        NexusCoreTestMod.init();
        NexusCoreCommonBootstrap.init();
    }
}
