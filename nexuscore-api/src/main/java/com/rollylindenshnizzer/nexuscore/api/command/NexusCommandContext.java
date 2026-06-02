package com.rollylindenshnizzer.nexuscore.api.command;

import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NexusCommandContext {
    private final Object nativeContext;
    private final NexusPlayer player;
    private final List<String> replies = new ArrayList<>();

    public NexusCommandContext(Object nativeContext, NexusPlayer player) {
        this.nativeContext = nativeContext;
        this.player = player;
    }

    public Object nativeContext() {
        return nativeContext;
    }

    public Optional<NexusPlayer> player() {
        return Optional.ofNullable(player);
    }

    public void reply(String message) {
        replies.add(message);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    public List<String> replies() {
        return new ArrayList<>(replies);
    }
}
