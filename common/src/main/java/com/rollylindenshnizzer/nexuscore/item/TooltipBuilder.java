package com.rollylindenshnizzer.nexuscore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public final class TooltipBuilder {
    private final List<Component> lines = new ArrayList<>();

    public TooltipBuilder line(Component component) {
        lines.add(component);
        return this;
    }

    public TooltipBuilder translatable(String key) {
        lines.add(Component.translatable(key));
        return this;
    }

    public TooltipBuilder colored(String key, ChatFormatting color) {
        MutableComponent component = Component.translatable(key);
        lines.add(component.withStyle(color));
        return this;
    }

    public TooltipBuilder energy(long amount, long capacity, String unit) {
        lines.add(Component.literal(amount + " / " + capacity + " " + unit));
        return this;
    }

    public List<Component> build() {
        return List.copyOf(lines);
    }
}
