package com.rollylindenshnizzer.nexuscore.gradle;

import com.rollylindenshnizzer.nexuscore.gradle.dsl.NexusTargetsSpec;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class NexusCoreExtension {
    private final Property<String> modId;
    private final Property<String> group;
    private final Property<String> version;
    private final Property<String> displayName;
    private final Property<String> description;
    private final ListProperty<String> authors;
    private final Property<String> license;
    private final Property<String> entrypointClass;
    private final NexusTargetsSpec targets;

    @Inject
    public NexusCoreExtension(ObjectFactory objects) {
        this.modId = objects.property(String.class);
        this.group = objects.property(String.class);
        this.version = objects.property(String.class);
        this.displayName = objects.property(String.class);
        this.description = objects.property(String.class);
        this.authors = objects.listProperty(String.class);
        this.license = objects.property(String.class);
        this.entrypointClass = objects.property(String.class);
        this.targets = objects.newInstance(NexusTargetsSpec.class, objects);

        this.version.convention("1.0.0");
        this.displayName.convention(this.modId);
        this.description.convention("");
        this.license.convention("MIT");
        this.entrypointClass.convention("");
        this.authors.convention(java.util.List.of());
    }

    public Property<String> getModId() {
        return modId;
    }

    public Property<String> getGroup() {
        return group;
    }

    public Property<String> getVersion() {
        return version;
    }

    public Property<String> getDisplayName() {
        return displayName;
    }

    public Property<String> getDescription() {
        return description;
    }

    public ListProperty<String> getAuthors() {
        return authors;
    }

    public Property<String> getLicense() {
        return license;
    }

    public Property<String> getEntrypointClass() {
        return entrypointClass;
    }

    public NexusTargetsSpec getTargets() {
        return targets;
    }

    public void targets(Action<? super NexusTargetsSpec> action) {
        action.execute(targets);
    }
}
