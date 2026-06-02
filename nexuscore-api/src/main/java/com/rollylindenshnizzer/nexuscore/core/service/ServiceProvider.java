package com.rollylindenshnizzer.nexuscore.core.service;

import com.rollylindenshnizzer.nexuscore.api.NexusLogger;
import com.rollylindenshnizzer.nexuscore.api.NexusPaths;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTargetCapabilities;
import com.rollylindenshnizzer.nexuscore.bridge.command.CommandBridge;
import com.rollylindenshnizzer.nexuscore.bridge.config.ConfigBridge;
import com.rollylindenshnizzer.nexuscore.bridge.datagen.DataGenBridge;
import com.rollylindenshnizzer.nexuscore.bridge.event.EventBridge;
import com.rollylindenshnizzer.nexuscore.bridge.network.NetworkBridge;
import com.rollylindenshnizzer.nexuscore.bridge.registry.BlockFactoryBridge;
import com.rollylindenshnizzer.nexuscore.bridge.registry.ItemFactoryBridge;
import com.rollylindenshnizzer.nexuscore.bridge.registry.RegistryBridge;

public interface ServiceProvider {
    RegistryBridge registries();

    EventBridge events();

    NetworkBridge networking();

    CommandBridge commands();

    ConfigBridge config();

    DataGenBridge datagen();

    ItemFactoryBridge itemFactory();

    BlockFactoryBridge blockFactory();

    NexusTarget target();

    NexusTargetCapabilities capabilities();

    NexusPaths paths();

    NexusLogger logger(String modId);
}
