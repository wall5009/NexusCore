package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;
import com.rollylindenshnizzer.nexuscore.api.network.NexusPacketContext;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide;

public final class SimplePacketContext implements NexusPacketContext {
    private final NexusPlayer player;
    private final NexusRuntimeSide side;

    public SimplePacketContext(NexusPlayer player, NexusRuntimeSide side) {
        this.player = player;
        this.side = side;
    }

    @Override
    public NexusPlayer player() {
        return player;
    }

    @Override
    public NexusRuntimeSide side() {
        return side;
    }

    @Override
    public void enqueue(Runnable task) {
        task.run();
    }
}
