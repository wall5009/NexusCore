package com.rollylindenshnizzer.nexuscore.event;

import dev.architectury.event.Event;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;

public final class NexusEvents {
    public static <T> void register(Event<T> event, T listener) {
        EventDiagnosticRegistry.mark(event.toString());
        event.register(listener);
    }

    public static void serverTick(TickEvent.Server listener) {
        EventDiagnosticRegistry.mark("server_tick_start");
        TickEvent.SERVER_PRE.register(listener);
    }

    public static void serverTickEnd(TickEvent.Server listener) {
        EventDiagnosticRegistry.mark("server_tick_end");
        TickEvent.SERVER_POST.register(listener);
    }

    public static void playerJoin(PlayerEvent.PlayerJoin listener) {
        EventDiagnosticRegistry.mark("player_join");
        PlayerEvent.PLAYER_JOIN.register(listener);
    }

    public static void playerQuit(PlayerEvent.PlayerQuit listener) {
        EventDiagnosticRegistry.mark("player_quit");
        PlayerEvent.PLAYER_QUIT.register(listener);
    }

    public static void blockBreak(BlockEvent.Break listener) {
        EventDiagnosticRegistry.mark("block_break");
        BlockEvent.BREAK.register(listener);
    }

    public static void rightClickBlock(InteractionEvent.RightClickBlock listener) {
        EventDiagnosticRegistry.mark("right_click_block");
        InteractionEvent.RIGHT_CLICK_BLOCK.register(listener);
    }

    public static void rightClickItem(InteractionEvent.RightClickItem listener) {
        EventDiagnosticRegistry.mark("right_click_item");
        InteractionEvent.RIGHT_CLICK_ITEM.register(listener);
    }

    private NexusEvents() {
    }
}
