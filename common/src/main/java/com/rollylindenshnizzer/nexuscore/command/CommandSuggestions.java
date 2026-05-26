package com.rollylindenshnizzer.nexuscore.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class CommandSuggestions {
    public static CompletableFuture<Suggestions> suggest(Collection<String> values, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(value);
            }
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> enumValues(Class<? extends Enum<?>> enumClass, SuggestionsBuilder builder) {
        return suggest(java.util.Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).map(String::toLowerCase).toList(), builder);
    }

    public static <S> String string(CommandContext<S> context, String name) {
        return context.getArgument(name, String.class);
    }

    private CommandSuggestions() {
    }
}
