package com.rollylindenshnizzer.nexuscore.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.function.BiPredicate;

public final class MenuBinding<M extends AbstractContainerMenu, T> {
    private final T target;
    private BiPredicate<Player, T> stillValid = (player, ignored) -> true;

    public MenuBinding(T target) {
        this.target = target;
    }

    public MenuBinding<M, T> stillValid(BiPredicate<Player, T> stillValid) {
        this.stillValid = stillValid;
        return this;
    }

    public boolean valid(Player player) {
        return stillValid.test(player, target);
    }

    public T target() {
        return target;
    }
}
