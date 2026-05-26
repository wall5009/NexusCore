package com.rollylindenshnizzer.nexuscore.block;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.client.ClientDescriptor;
import com.rollylindenshnizzer.nexuscore.client.NexusClientDescriptors;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public class BlockBuilder<B extends Block> {
    private final String modId;
    private final String path;
    private BlockBehaviour.Properties properties = BlockBehaviour.Properties.of();
    private Function<BlockBehaviour.Properties, B> factory;
    private boolean blockItem;
    private Item.Properties blockItemProperties = new Item.Properties();
    private DeferredSupplier<CreativeModeTab> creativeTab;
    private ResourceKey<CreativeModeTab> creativeTabKey;
    private boolean simpleCubeModel;
    private boolean dropsSelf;
    private boolean generatedLanguage = true;
    private RegistrySupplier<Item> registeredBlockItem;
    private Integer defaultColor;
    private String renderLayer;

    @SuppressWarnings("unchecked")
    public BlockBuilder(String modId, String path) {
        this.modId = NexusIds.requireNamespace(modId);
        this.path = NexusIds.normalizePath(path);
        this.factory = properties -> (B) new Block(properties);
    }

    public BlockBuilder<B> properties(BlockBehaviour.Properties properties) {
        this.properties = properties;
        return this;
    }

    public BlockBuilder<B> factory(Function<BlockBehaviour.Properties, B> factory) {
        this.factory = factory;
        return this;
    }

    public BlockBuilder<B> strength(float hardness, float resistance) {
        properties.strength(hardness, resistance);
        return this;
    }

    public BlockBuilder<B> strength(float hardness) {
        properties.strength(hardness);
        return this;
    }

    public BlockBuilder<B> requiresCorrectTool() {
        properties.requiresCorrectToolForDrops();
        return this;
    }

    public BlockBuilder<B> mapColor(MapColor color) {
        properties.mapColor(color);
        return this;
    }

    public BlockBuilder<B> sound(SoundType soundType) {
        properties.sound(soundType);
        return this;
    }

    public BlockBuilder<B> lightLevel(int light) {
        properties.lightLevel(state -> light);
        return this;
    }

    public BlockBuilder<B> noOcclusion() {
        properties.noOcclusion();
        return this;
    }

    public BlockBuilder<B> withBlockItem() {
        this.blockItem = true;
        return this;
    }

    public BlockBuilder<B> blockItemProperties(Item.Properties properties) {
        this.blockItemProperties = properties;
        return this;
    }

    public BlockBuilder<B> creativeTab(DeferredSupplier<CreativeModeTab> tab) {
        this.creativeTab = tab;
        return this;
    }

    public BlockBuilder<B> creativeTab(ResourceKey<CreativeModeTab> tab) {
        this.creativeTabKey = tab;
        return this;
    }

    public BlockBuilder<B> simpleCubeModel() {
        this.simpleCubeModel = true;
        return this;
    }

    public BlockBuilder<B> dropsSelf() {
        this.dropsSelf = true;
        return this;
    }

    public BlockBuilder<B> mineableWithPickaxe() {
        NexusData.plan(modId).tag("blocks", "mineable/pickaxe", modId + ":" + path);
        return this;
    }

    public BlockBuilder<B> mineableWithAxe() {
        NexusData.plan(modId).tag("blocks", "mineable/axe", modId + ":" + path);
        return this;
    }

    public BlockBuilder<B> mineableWithShovel() {
        NexusData.plan(modId).tag("blocks", "mineable/shovel", modId + ":" + path);
        return this;
    }

    public BlockBuilder<B> needsStoneTool() {
        NexusData.plan(modId).tag("blocks", "needs_stone_tool", modId + ":" + path);
        return this;
    }

    public BlockBuilder<B> needsIronTool() {
        NexusData.plan(modId).tag("blocks", "needs_iron_tool", modId + ":" + path);
        return this;
    }

    public BlockBuilder<B> needsDiamondTool() {
        NexusData.plan(modId).tag("blocks", "needs_diamond_tool", modId + ":" + path);
        return this;
    }

    public BlockBuilder<B> renderLayer(String layer) {
        this.renderLayer = layer;
        return this;
    }

    public BlockBuilder<B> defaultColor(int color) {
        this.defaultColor = color;
        return this;
    }

    public BlockBuilder<B> noGeneratedLanguage() {
        this.generatedLanguage = false;
        return this;
    }

    public RegistrySupplier<B> register() {
        RegistrySupplier<B> block = NexusRegistries.group(modId).blocks().register(path, () -> factory.apply(properties));
        if (blockItem) {
            registeredBlockItem = NexusRegistries.group(modId).items().register(path, () -> new BlockItem(block.get(), blockItemProperties));
            if (creativeTab != null) {
                CreativeTabRegistry.append(creativeTab, registeredBlockItem);
            }
            if (creativeTabKey != null) {
                CreativeTabRegistry.append(creativeTabKey, registeredBlockItem);
            }
        }
        if (simpleCubeModel) {
            NexusData.plan(modId).blockCubeAll(path);
        }
        if (dropsSelf) {
            NexusData.plan(modId).lootDropsSelf(path);
        }
        if (generatedLanguage) {
            NexusData.plan(modId).translation("block." + modId + "." + path.replace('/', '.'), NexusIds.humanName(path));
        }
        if (renderLayer != null) {
            NexusClientDescriptors.register(new ClientDescriptor.RenderLayer("block/" + path, NexusIds.id(modId, path), renderLayer));
        }
        if (defaultColor != null) {
            NexusClientDescriptors.register(new ClientDescriptor.ColorProvider("block/" + path, NexusIds.id(modId, path), defaultColor));
        }
        return block;
    }

    public RegistrySupplier<Item> registeredBlockItem() {
        return registeredBlockItem;
    }
}
