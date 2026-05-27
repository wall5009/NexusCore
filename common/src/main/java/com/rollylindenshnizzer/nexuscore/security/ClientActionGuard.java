package com.rollylindenshnizzer.nexuscore.security;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@NexusStable(since = "1.2")
public final class ClientActionGuard<T> {
    private final List<Rule<T>> rules = new ArrayList<>();

    public ClientActionGuard<T> allow(String name, Predicate<T> predicate) {
        rules.add(new Rule<>(name, predicate));
        return this;
    }

    public Result validate(T action) {
        for (Rule<T> rule : rules) {
            if (!rule.predicate().test(action)) {
                return new Result(false, rule.name());
            }
        }
        return new Result(true, "allowed");
    }

    private record Rule<T>(String name, Predicate<T> predicate) {
    }

    public record Result(boolean allowed, String reason) {
    }
}
