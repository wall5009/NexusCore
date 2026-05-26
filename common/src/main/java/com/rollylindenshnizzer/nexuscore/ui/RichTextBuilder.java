package com.rollylindenshnizzer.nexuscore.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class RichTextBuilder {
    private MutableComponent component = Component.empty();

    public RichTextBuilder literal(String text) {
        component.append(Component.literal(text));
        return this;
    }

    public RichTextBuilder translatable(String key, Object... args) {
        component.append(Component.translatable(key, args));
        return this;
    }

    public RichTextBuilder color(ChatFormatting formatting) {
        component = component.withStyle(formatting);
        return this;
    }

    public Component build() {
        return component;
    }
}
