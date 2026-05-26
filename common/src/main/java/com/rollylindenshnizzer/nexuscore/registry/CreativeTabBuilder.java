package com.rollylindenshnizzer.nexuscore.registry;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class CreativeTabBuilder {
    private final String modId;
    private final String path;
    private Component title;
    private Supplier<ItemStack> icon = () -> new ItemStack(Items.BOOK);
    private final List<Supplier<? extends ItemLike>> entries = new ArrayList<>();

    public CreativeTabBuilder(String modId, String path) {
        this.modId = NexusIds.requireNamespace(modId);
        this.path = NexusIds.normalizePath(path);
        this.title = Component.translatable("itemGroup." + modId + "." + path.replace('/', '.'));
    }

    public CreativeTabBuilder title(Component title) {
        this.title = title;
        return this;
    }

    public CreativeTabBuilder icon(Supplier<ItemStack> icon) {
        this.icon = icon;
        return this;
    }

    public CreativeTabBuilder entry(Supplier<? extends ItemLike> entry) {
        this.entries.add(entry);
        return this;
    }

    public RegistrySupplier<CreativeModeTab> register() {
        RegistrySupplier<CreativeModeTab> tab = NexusRegistries.group(modId).creativeTabs().register(path, () ->
                CreativeTabRegistry.create(builder -> builder.title(title).icon(icon).displayItems((parameters, output) -> {
                    for (Supplier<? extends ItemLike> entry : entries) {
                        output.accept(entry.get());
                    }
                })));
        return tab;
    }
}
