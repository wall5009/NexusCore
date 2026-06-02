package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.command.NexusCommandContext;
import com.rollylindenshnizzer.nexuscore.api.command.NexusCommandDefinition;
import com.rollylindenshnizzer.nexuscore.bridge.command.CommandBridge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryCommandBridge implements CommandBridge {
    private final Map<String, NexusCommandDefinition> commands = new LinkedHashMap<>();

    @Override
    public void register(NexusCommandDefinition definition) {
        if (commands.putIfAbsent(definition.literal(), definition) != null) {
            throw new IllegalStateException("command literal is already registered: " + definition.literal());
        }
    }

    @Override
    public Optional<NexusCommandDefinition> find(String literal) {
        return Optional.ofNullable(commands.get(literal));
    }

    @Override
    public int execute(String literal, NexusCommandContext context) {
        NexusCommandDefinition definition = commands.get(literal);
        if (definition == null || definition.executor() == null) {
            return 0;
        }
        if (!definition.requirement().test(context)) {
            context.reply("You do not have permission to run /" + literal + ".");
            return 0;
        }
        return definition.executor().run(context);
    }

    @Override
    public List<NexusCommandDefinition> commands() {
        return new ArrayList<>(commands.values());
    }
}
