package com.rollylindenshnizzer.nexuscore.ui.binding;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@NexusStable(since = "1.2")
public final class ObservableValue<T> {
    private final List<Consumer<T>> listeners = new ArrayList<>();
    private T value;

    public ObservableValue(T initialValue) {
        this.value = initialValue;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        if (Objects.equals(this.value, value)) {
            return;
        }
        this.value = value;
        listeners.forEach(listener -> listener.accept(value));
    }

    public ObservableValue<T> onChanged(Consumer<T> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        return this;
    }
}
