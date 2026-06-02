package com.rollylindenshnizzer.nexuscore.api.command;

@FunctionalInterface
public interface NexusCommandExecutor {
    int run(NexusCommandContext context);
}
