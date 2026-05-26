package com.rollylindenshnizzer.nexuscore.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public final class MiniMarkup {
    public static Component parse(String input) {
        MutableComponent result = Component.empty();
        ChatFormatting active = ChatFormatting.WHITE;
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '[') {
                int end = input.indexOf(']', i);
                if (end > i) {
                    flush(result, buffer, active);
                    active = color(input.substring(i + 1, end), active);
                    i = end;
                    continue;
                }
            }
            buffer.append(c);
        }
        flush(result, buffer, active);
        return result;
    }

    private static ChatFormatting color(String name, ChatFormatting fallback) {
        try {
            return ChatFormatting.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static void flush(MutableComponent result, StringBuilder buffer, ChatFormatting formatting) {
        if (!buffer.isEmpty()) {
            result.append(Component.literal(buffer.toString()).withStyle(formatting));
            buffer.setLength(0);
        }
    }

    private MiniMarkup() {
    }
}
