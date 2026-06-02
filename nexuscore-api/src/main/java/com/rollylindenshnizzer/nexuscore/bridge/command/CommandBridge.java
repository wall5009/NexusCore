package com.rollylindenshnizzer.nexuscore.bridge.command;

import com.rollylindenshnizzer.nexuscore.api.command.NexusCommandContext;
import com.rollylindenshnizzer.nexuscore.api.command.NexusCommandDefinition;

import java.util.List;
import java.util.Optional;

public interface CommandBridge {
    void register(NexusCommandDefinition definition);

    Optional<NexusCommandDefinition> find(String literal);

    int execute(String literal, NexusCommandContext context);

    List<NexusCommandDefinition> commands();
}
