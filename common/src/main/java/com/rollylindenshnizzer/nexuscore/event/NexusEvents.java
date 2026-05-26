package com.rollylindenshnizzer.nexuscore.event;

import dev.architectury.event.Event;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;

public final class NexusEvents {
    public static <T> void register(Event<T> event, T listener) {
        event.register(listener);
    }

    public static void serverTick(TickEvent.Server listener) {
        TickEvent.SERVER_PRE.register(listener);
    }

    public static void serverTickEnd(TickEvent.Server listener) {
        TickEvent.SERVER_POST.register(listener);
    }

    public static void playerJoin(PlayerEvent.PlayerJoin listener) {
        PlayerEvent.PLAYER_JOIN.register(listener);
    }

    public static void playerQuit(PlayerEvent.PlayerQuit listener) {
        PlayerEvent.PLAYER_QUIT.register(listener);
    }

    public static void blockBreak(BlockEvent.Break listener) {
        BlockEvent.BREAK.register(listener);
    }

    public static void rightClickBlock(InteractionEvent.RightClickBlock listener) {
        InteractionEvent.RIGHT_CLICK_BLOCK.register(listener);
    }

    public static void rightClickItem(InteractionEvent.RightClickItem listener) {
        InteractionEvent.RIGHT_CLICK_ITEM.register(listener);
    }

    private NexusEvents() {
    }
}
