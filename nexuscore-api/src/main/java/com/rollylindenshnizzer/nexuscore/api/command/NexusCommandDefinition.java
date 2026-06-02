package com.rollylindenshnizzer.nexuscore.api.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class NexusCommandDefinition {
    private final String literal;
    private final String permission;
    private final Predicate<NexusCommandContext> requirement;
    private final NexusCommandExecutor executor;
    private final List<NexusCommandDefinition> children;

    public NexusCommandDefinition(String literal, String permission, Predicate<NexusCommandContext> requirement, NexusCommandExecutor executor, List<NexusCommandDefinition> children) {
        this.literal = Objects.requireNonNull(literal, "literal");
        this.permission = permission;
        this.requirement = requirement == null ? ignored -> true : requirement;
        this.executor = executor;
        this.children = List.copyOf(children);
    }

    public String literal() {
        return literal;
    }

    public String permission() {
        return permission;
    }

    public Predicate<NexusCommandContext> requirement() {
        return requirement;
    }

    public NexusCommandExecutor executor() {
        return executor;
    }

    public List<NexusCommandDefinition> children() {
        return new ArrayList<>(children);
    }
}
