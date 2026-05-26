package com.rollylindenshnizzer.nexuscore.compat;

import java.util.Collection;

public final class NexusCompat {
    public static void initialize(Collection<? extends CompatModule> modules) {
        for (CompatModule module : modules) {
            if (module.loaded()) {
                module.initialize();
            }
        }
    }

    public static boolean canLoadClass(String className) {
        try {
            Class.forName(className, false, NexusCompat.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private NexusCompat() {
    }
}
