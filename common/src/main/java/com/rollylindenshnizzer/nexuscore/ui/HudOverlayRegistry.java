package com.rollylindenshnizzer.nexuscore.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class HudOverlayRegistry {
    private static final List<Overlay> OVERLAYS = new ArrayList<>();

    public static void register(String id, Anchor anchor, BooleanSupplier visible) {
        OVERLAYS.add(new Overlay(id, anchor, visible));
    }

    public static List<Overlay> overlays() {
        return List.copyOf(OVERLAYS);
    }

    public enum Anchor {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER
    }

    public record Overlay(String id, Anchor anchor, BooleanSupplier visible) {
        public boolean shouldRender() {
            return visible.getAsBoolean();
        }
    }

    private HudOverlayRegistry() {
    }
}
