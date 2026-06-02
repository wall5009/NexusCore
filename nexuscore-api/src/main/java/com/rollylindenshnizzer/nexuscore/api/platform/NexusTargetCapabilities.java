package com.rollylindenshnizzer.nexuscore.api.platform;

import java.util.Set;

public interface NexusTargetCapabilities {
    boolean supports(NexusFeature feature);

    Set<NexusFeature> features();
}
