package com.rollylindenshnizzer.nexuscore.compat;

import java.util.Optional;

public final class SafeClassloading {
    public static Optional<Class<?>> find(String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException exception) {
            return Optional.empty();
        }
    }

    public static boolean present(String className) {
        return find(className).isPresent();
    }

    private SafeClassloading() {
    }
}
