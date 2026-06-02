package com.rollylindenshnizzer.nexuscore.gradle.dsl;

import com.rollylindenshnizzer.nexuscore.gradle.target.NexusTarget;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class NexusTargetsSpec {
    private final ObjectFactory objects;
    private final List<NexusMinecraftTargetSpec> minecraftTargets = new ArrayList<>();

    @Inject
    public NexusTargetsSpec(ObjectFactory objects) {
        this.objects = objects;
    }

    public void minecraft(String version, Action<? super NexusMinecraftTargetSpec> action) {
        NexusMinecraftTargetSpec spec = objects.newInstance(NexusMinecraftTargetSpec.class, version);
        action.execute(spec);
        minecraftTargets.add(spec);
    }

    public List<NexusTarget> resolveTargets() {
        return minecraftTargets.stream().flatMap(spec -> spec.targets().stream()).toList();
    }

    public boolean isEmpty() {
        return minecraftTargets.isEmpty();
    }
}
