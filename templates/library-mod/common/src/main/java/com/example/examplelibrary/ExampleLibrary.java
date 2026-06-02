package com.example.examplelibrary;

import com.rollylindenshnizzer.nexuscore.api.NexusMod;

public final class ExampleLibrary {
    public static final NexusMod MOD = NexusMod.create("examplelibrary");

    private ExampleLibrary() {
    }

    public static void init() {
        MOD.logger().info("Example library loaded.");
    }
}
