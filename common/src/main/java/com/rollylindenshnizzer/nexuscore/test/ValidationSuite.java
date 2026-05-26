package com.rollylindenshnizzer.nexuscore.test;

import java.util.ArrayList;
import java.util.List;

public final class ValidationSuite {
    private final List<Check> checks = new ArrayList<>();

    public ValidationSuite check(String name, Runnable check) {
        checks.add(new Check(name, check));
        return this;
    }

    public Result run() {
        List<String> failures = new ArrayList<>();
        for (Check check : checks) {
            try {
                check.runnable().run();
            } catch (RuntimeException | AssertionError error) {
                failures.add(check.name() + ": " + error.getMessage());
            }
        }
        return new Result(checks.size(), failures);
    }

    private record Check(String name, Runnable runnable) {
    }

    public record Result(int total, List<String> failures) {
        public boolean passed() {
            return failures.isEmpty();
        }
    }
}
