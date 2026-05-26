package com.rollylindenshnizzer.nexuscore.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class NexusCommands {
    private static final List<LiteralArgumentBuilder<CommandSourceStack>> COMMANDS = new ArrayList<>();
    private static boolean installed;

    public static CommandBuilder literal(String name) {
        return new CommandBuilder(name);
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
        COMMANDS.add(command);
        install();
    }

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        CommandRegistrationEvent.EVENT.register((dispatcher, context, selection) -> {
            for (LiteralArgumentBuilder<CommandSourceStack> command : COMMANDS) {
                dispatcher.register(command);
            }
        });
    }

    private NexusCommands() {
    }

    public static final class CommandBuilder {
        private final LiteralArgumentBuilder<CommandSourceStack> builder;

        private CommandBuilder(String name) {
            this.builder = Commands.literal(name);
        }

        public CommandBuilder permission(int level) {
            builder.requires(source -> source.hasPermission(level));
            return this;
        }

        public CommandBuilder requires(Predicate<CommandSourceStack> predicate) {
            builder.requires(predicate);
            return this;
        }

        public CommandBuilder executes(Command<CommandSourceStack> command) {
            builder.executes(command);
            return this;
        }

        public CommandBuilder feedback(Component message) {
            builder.executes(context -> {
                context.getSource().sendSuccess(() -> message, false);
                return 1;
            });
            return this;
        }

        public LiteralArgumentBuilder<CommandSourceStack> build() {
            return builder;
        }

        public void register() {
            NexusCommands.register(builder);
        }
    }
}
