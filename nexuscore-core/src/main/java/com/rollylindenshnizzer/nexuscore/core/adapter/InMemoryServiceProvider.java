package com.rollylindenshnizzer.nexuscore.core.adapter;

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
import com.rollylindenshnizzer.nexuscore.core.logging.ConsoleNexusLogger;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.target.SimpleTargetCapabilities;

import java.nio.file.Path;

public class InMemoryServiceProvider implements ServiceProvider {
    private final NexusTarget target;
    private final NexusTargetCapabilities capabilities;
    private final NexusPaths paths;
    private final RegistryBridge registries;
    private final EventBridge events;
    private final NetworkBridge networking;
    private final CommandBridge commands;
    private final ConfigBridge config;
    private final DataGenBridge datagen;
    private final ItemFactoryBridge itemFactory;
    private final BlockFactoryBridge blockFactory;

    public InMemoryServiceProvider(NexusTarget target, NexusTargetCapabilities capabilities, NexusPaths paths) {
        this(target, capabilities, paths, null, null, null, null, null, null);
    }

    protected InMemoryServiceProvider(NexusTarget target, NexusTargetCapabilities capabilities, NexusPaths paths, RegistryBridge registries, ItemFactoryBridge itemFactory, BlockFactoryBridge blockFactory) {
        this(target, capabilities, paths, registries, null, null, null, itemFactory, blockFactory);
    }

    protected InMemoryServiceProvider(NexusTarget target, NexusTargetCapabilities capabilities, NexusPaths paths, RegistryBridge registries, EventBridge events, NetworkBridge networking, CommandBridge commands, ItemFactoryBridge itemFactory, BlockFactoryBridge blockFactory) {
        this.target = target;
        this.capabilities = capabilities == null ? SimpleTargetCapabilities.allStable() : capabilities;
        this.paths = paths == null ? NexusPaths.under(Path.of(".")) : paths;
        this.registries = registries == null ? new InMemoryRegistryBridge(target) : registries;
        this.events = events == null ? new InMemoryEventBridge() : events;
        this.networking = networking == null ? new InMemoryNetworkBridge() : networking;
        this.commands = commands == null ? new InMemoryCommandBridge() : commands;
        this.config = new FileConfigBridge(this.paths);
        this.datagen = new FileDataGenBridge(this.paths);
        this.itemFactory = itemFactory == null ? new SimpleItemFactoryBridge() : itemFactory;
        this.blockFactory = blockFactory == null ? new SimpleBlockFactoryBridge() : blockFactory;
    }

    @Override
    public RegistryBridge registries() {
        return registries;
    }

    @Override
    public EventBridge events() {
        return events;
    }

    @Override
    public NetworkBridge networking() {
        return networking;
    }

    @Override
    public CommandBridge commands() {
        return commands;
    }

    @Override
    public ConfigBridge config() {
        return config;
    }

    @Override
    public DataGenBridge datagen() {
        return datagen;
    }

    @Override
    public ItemFactoryBridge itemFactory() {
        return itemFactory;
    }

    @Override
    public BlockFactoryBridge blockFactory() {
        return blockFactory;
    }

    @Override
    public NexusTarget target() {
        return target;
    }

    @Override
    public NexusTargetCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public NexusPaths paths() {
        return paths;
    }

    @Override
    public NexusLogger logger(String modId) {
        return new ConsoleNexusLogger(modId);
    }
}
