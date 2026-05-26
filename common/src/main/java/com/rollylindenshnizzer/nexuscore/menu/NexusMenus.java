package com.rollylindenshnizzer.nexuscore.menu;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

@SuppressWarnings("unchecked")
public final class NexusMenus {
    public static <T extends AbstractContainerMenu> RegistrySupplier<MenuType<T>> menu(String modId, String path, MenuType.MenuSupplier<T> supplier) {
        String normalized = NexusIds.normalizePath(path);
        return (RegistrySupplier<MenuType<T>>) (RegistrySupplier<?>) NexusRegistries.group(modId).menus()
                .register(normalized, () -> new MenuType<>(supplier, FeatureFlags.DEFAULT_FLAGS));
    }

    private NexusMenus() {
    }
}
