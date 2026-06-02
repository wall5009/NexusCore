package com.rollylindenshnizzer.nexuscore.bridge.config;

import com.rollylindenshnizzer.nexuscore.api.config.NexusConfig;

import java.nio.file.Path;

public interface ConfigBridge {
    Path configPath(String modId);

    void loadOrCreate(NexusConfig config);

    void save(NexusConfig config);
}
