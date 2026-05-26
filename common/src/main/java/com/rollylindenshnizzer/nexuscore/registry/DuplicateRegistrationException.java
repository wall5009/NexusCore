package com.rollylindenshnizzer.nexuscore.registry;

import com.rollylindenshnizzer.nexuscore.core.NexusException;

public final class DuplicateRegistrationException extends NexusException {
    public DuplicateRegistrationException(String modId, String registryName, String path) {
        super("Duplicate " + registryName + " registration: " + modId + ":" + path
                + ". Give each registered object a unique path.");
    }
}
