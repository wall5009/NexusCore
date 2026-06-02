package com.rollylindenshnizzer.nexuscore.api.event;

import java.util.ArrayList;
import java.util.List;

public final class NexusPlayer {
    private final Object nativePlayer;
    private final String name;
    private final List<String> messages = new ArrayList<>();

    public NexusPlayer(Object nativePlayer, String name) {
        this.nativePlayer = nativePlayer;
        this.name = name == null || name.isBlank() ? "unknown" : name;
    }

    public Object nativePlayer() {
        return nativePlayer;
    }

    public String name() {
        return name;
    }

    public void sendMessage(String message) {
        messages.add(message);
    }

    public void openMenu(String menuId) {
        messages.add("open_menu:" + menuId);
    }

    public List<String> messages() {
        return new ArrayList<>(messages);
    }
}
