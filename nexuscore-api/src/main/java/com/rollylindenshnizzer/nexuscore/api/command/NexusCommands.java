package com.rollylindenshnizzer.nexuscore.api.command;

import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class NexusCommands {
    private NexusCommands() {
    }

    public static LiteralBuilder literal(String literal) {
        return new LiteralBuilder(literal);
    }

    public static final class LiteralBuilder {
        private final String literal;
        private String permission;
        private Predicate<NexusCommandContext> requirement = ignored -> true;
        private NexusCommandExecutor executor;
        private final List<NexusCommandDefinition> children = new ArrayList<>();

        private LiteralBuilder(String literal) {
            this.literal = Objects.requireNonNull(literal, "literal");
        }

        public LiteralBuilder permission(String permission) {
            this.permission = Objects.requireNonNull(permission, "permission");
            return this;
        }

        public LiteralBuilder requires(Predicate<NexusCommandContext> requirement) {
            this.requirement = Objects.requireNonNull(requirement, "requirement");
            return this;
        }

        public LiteralBuilder executes(NexusCommandExecutor executor) {
            this.executor = Objects.requireNonNull(executor, "executor");
            return this;
        }

        public LiteralBuilder then(LiteralBuilder child) {
            this.children.add(child.build());
            return this;
        }

        public NexusCommandDefinition build() {
            return new NexusCommandDefinition(literal, permission, requirement, executor, children);
        }

        public NexusCommandDefinition register() {
            NexusCommandDefinition definition = build();
            NexusServices.get().commands().register(definition);
            return definition;
        }
    }
}
