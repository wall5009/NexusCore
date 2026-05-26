package com.rollylindenshnizzer.nexuscore.inventory;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class QuickMoveRouter {
    private final List<Route> routes = new ArrayList<>();

    public QuickMoveRouter route(SlotRange from, SlotRange to, Predicate<ItemStack> predicate) {
        routes.add(new Route(from, to, predicate));
        return this;
    }

    public List<Route> routesFor(int slot, ItemStack stack) {
        return routes.stream()
                .filter(route -> route.from().contains(slot))
                .filter(route -> route.predicate().test(stack))
                .toList();
    }

    public record Route(SlotRange from, SlotRange to, Predicate<ItemStack> predicate) {
    }
}
