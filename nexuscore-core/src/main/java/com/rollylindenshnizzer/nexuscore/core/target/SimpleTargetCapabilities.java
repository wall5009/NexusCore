package com.rollylindenshnizzer.nexuscore.core.target;

import com.rollylindenshnizzer.nexuscore.api.platform.NexusFeature;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTargetCapabilities;

import java.util.EnumSet;
import java.util.Set;

public final class SimpleTargetCapabilities implements NexusTargetCapabilities {
    private final EnumSet<NexusFeature> features;

    private SimpleTargetCapabilities(EnumSet<NexusFeature> features) {
        this.features = features.clone();
    }

    public static SimpleTargetCapabilities of(NexusFeature... features) {
        EnumSet<NexusFeature> set = EnumSet.noneOf(NexusFeature.class);
        for (NexusFeature feature : features) {
            set.add(feature);
        }
        return new SimpleTargetCapabilities(set);
    }

    public static SimpleTargetCapabilities allStable() {
        return new SimpleTargetCapabilities(EnumSet.allOf(NexusFeature.class));
    }

    @Override
    public boolean supports(NexusFeature feature) {
        return features.contains(feature);
    }

    @Override
    public Set<NexusFeature> features() {
        return EnumSet.copyOf(features);
    }
}
