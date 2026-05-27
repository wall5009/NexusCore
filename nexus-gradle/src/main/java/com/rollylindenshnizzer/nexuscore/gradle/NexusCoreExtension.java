package com.rollylindenshnizzer.nexuscore.gradle;

import org.gradle.api.provider.Property;

public abstract class NexusCoreExtension {
    public abstract Property<String> getModId();

    public abstract Property<String> getMinecraftVersion();

    public abstract Property<String> getLoaderSet();
}
