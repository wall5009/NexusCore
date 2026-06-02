package com.rollylindenshnizzer.nexuscore.api.network;

import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide;

public interface NexusPacketContext {
    NexusPlayer player();

    NexusRuntimeSide side();

    void enqueue(Runnable task);
}
