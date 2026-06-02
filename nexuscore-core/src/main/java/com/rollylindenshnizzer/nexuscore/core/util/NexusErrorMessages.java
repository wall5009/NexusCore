package com.rollylindenshnizzer.nexuscore.core.util;

import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;

public final class NexusErrorMessages {
    private NexusErrorMessages() {
    }

    public static IllegalStateException failure(String system, String subject, NexusTarget target, String reason, String fix) {
        return new IllegalStateException("NexusCore could not " + system + " '" + subject + "'. Target: " + target.targetId() + ". Reason: " + reason + ". Fix: " + fix);
    }
}
