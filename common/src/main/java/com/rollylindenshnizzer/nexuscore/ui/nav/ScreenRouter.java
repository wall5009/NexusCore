package com.rollylindenshnizzer.nexuscore.ui.nav;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Consumer;

public final class ScreenRouter<S> {
    private final Deque<S> backStack = new ArrayDeque<>();
    private final Consumer<S> opener;
    private S current;

    public ScreenRouter(Consumer<S> opener) {
        this.opener = opener;
    }

    public void open(S screen) {
        if (current != null) {
            backStack.push(current);
        }
        current = screen;
        opener.accept(screen);
    }

    public Optional<S> back() {
        if (backStack.isEmpty()) {
            return Optional.empty();
        }
        current = backStack.pop();
        opener.accept(current);
        return Optional.of(current);
    }

    public Breadcrumb breadcrumbs() {
        return new Breadcrumb(backStack.stream().map(Object::toString).toList());
    }

    public record Breadcrumb(java.util.List<String> entries) {
        public String display() {
            return String.join(" > ", entries);
        }
    }
}
