package com.rollylindenshnizzer.nexuscore.gradle.task;

import com.rollylindenshnizzer.nexuscore.gradle.target.NexusTarget;
import com.rollylindenshnizzer.nexuscore.gradle.target.NexusSupportedTargets;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;

@DisableCachingByDefault(because = "Target validation is lightweight and should always reflect the current DSL input.")
public abstract class ValidateNexusTargetsTask extends DefaultTask {
    @Inject
    public ValidateNexusTargetsTask() {
    }

    @Input
    public abstract ListProperty<String> getTargetIds();

    @TaskAction
    public void validate() {
        for (String targetId : getTargetIds().get()) {
            String[] parts = targetId.split("_", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid NexusCore target id '" + targetId + "'. Expected loader_version.");
            }
            NexusSupportedTargets.validate(new NexusTarget(parts[0], parts[1].replace('_', '.')));
        }
        getLogger().lifecycle("Validated NexusCore targets: {}", getTargetIds().get());
    }
}
