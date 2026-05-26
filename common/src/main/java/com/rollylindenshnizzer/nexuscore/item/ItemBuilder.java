package com.rollylindenshnizzer.nexuscore.item;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.client.ClientDescriptor;
import com.rollylindenshnizzer.nexuscore.client.NexusClientDescriptors;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.fuel.FuelRegistry;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemBuilder<I extends Item> {
    private final String modId;
    private final String path;
    private Item.Properties properties = new Item.Properties();
    private Function<Item.Properties, I> factory;
    private final List<Component> tooltips = new ArrayList<>();
    private DeferredSupplier<CreativeModeTab> creativeTab;
    private ResourceKey<CreativeModeTab> creativeTabKey;
    private boolean generatedModel;
    private boolean generatedLanguage = true;
    private Integer fuelTicks;
    private NexusItem.UseHandler useHandler;
    private Integer defaultColor;

    @SuppressWarnings("unchecked")
    public ItemBuilder(String modId, String path) {
        this.modId = NexusIds.requireNamespace(modId);
        this.path = NexusIds.normalizePath(path);
        this.factory = properties -> (I) new NexusItem(properties, List.copyOf(tooltips), useHandler);
    }

    public ItemBuilder<I> properties(Item.Properties properties) {
        this.properties = properties;
        return this;
    }

    public ItemBuilder<I> factory(Function<Item.Properties, I> factory) {
        this.factory = factory;
        return this;
    }

    public ItemBuilder<I> stackSize(int size) {
        properties.stacksTo(size);
        return this;
    }

    public ItemBuilder<I> durability(int durability) {
        properties.durability(durability);
        return this;
    }

    public ItemBuilder<I> rarity(Rarity rarity) {
        properties.rarity(rarity);
        return this;
    }

    public ItemBuilder<I> fireResistant() {
        properties.fireResistant();
        return this;
    }

    public ItemBuilder<I> food(FoodProperties food) {
        properties.food(food);
        return this;
    }

    public ItemBuilder<I> tooltip(String translationKey) {
        tooltips.add(Component.translatable(translationKey));
        return this;
    }

    public ItemBuilder<I> tooltip(Component line) {
        tooltips.add(line);
        return this;
    }

    public ItemBuilder<I> creativeTab(DeferredSupplier<CreativeModeTab> creativeTab) {
        this.creativeTab = creativeTab;
        return this;
    }

    public ItemBuilder<I> creativeTab(ResourceKey<CreativeModeTab> creativeTabKey) {
        this.creativeTabKey = creativeTabKey;
        return this;
    }

    public ItemBuilder<I> modelGenerated() {
        this.generatedModel = true;
        return this;
    }

    public ItemBuilder<I> noGeneratedLanguage() {
        this.generatedLanguage = false;
        return this;
    }

    public ItemBuilder<I> fuel(int ticks) {
        this.fuelTicks = ticks;
        return this;
    }

    public ItemBuilder<I> use(NexusItem.UseHandler useHandler) {
        this.useHandler = useHandler;
        return this;
    }

    public ItemBuilder<I> defaultColor(int color) {
        this.defaultColor = color;
        return this;
    }

    public ItemBuilder<I> compostable(float chance) {
        NexusData.plan(modId).translation("nexuscore.compostable." + modId + "." + path.replace('/', '.'), Float.toString(chance));
        return this;
    }

    public RegistrySupplier<I> register() {
        RegistrySupplier<I> item = NexusRegistries.group(modId).items().register(path, () -> factory.apply(properties));
        if (creativeTab != null) {
            CreativeTabRegistry.append(creativeTab, item);
        }
        if (creativeTabKey != null) {
            CreativeTabRegistry.append(creativeTabKey, item);
        }
        if (fuelTicks != null) {
            item.listen(value -> FuelRegistry.register(fuelTicks, value));
        }
        if (generatedModel) {
            NexusData.plan(modId).itemGenerated(path);
        }
        if (generatedLanguage) {
            NexusData.plan(modId).translation("item." + modId + "." + path.replace('/', '.'), NexusIds.humanName(path));
        }
        if (defaultColor != null) {
            NexusClientDescriptors.register(new ClientDescriptor.ColorProvider("item/" + path, NexusIds.id(modId, path), defaultColor));
        }
        return item;
    }
}
