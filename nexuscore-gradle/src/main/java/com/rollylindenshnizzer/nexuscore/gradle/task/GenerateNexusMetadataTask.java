package com.rollylindenshnizzer.nexuscore.gradle.task;

import com.rollylindenshnizzer.nexuscore.gradle.metadata.NexusMetadataModel;
import com.rollylindenshnizzer.nexuscore.gradle.metadata.NexusMetadataWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.IOException;

@DisableCachingByDefault(because = "Generates small deterministic metadata files and has no expensive work to cache.")
public abstract class GenerateNexusMetadataTask extends DefaultTask {
    @Inject
    public GenerateNexusMetadataTask() {
    }

    @Input
    public abstract Property<String> getModId();

    @Input
    public abstract Property<String> getDisplayName();

    @Input
    public abstract Property<String> getModDescription();

    @Input
    public abstract Property<String> getModVersion();

    @Input
    public abstract Property<String> getLicenseName();

    @Input
    public abstract ListProperty<String> getAuthors();

    @Input
    public abstract Property<String> getLoader();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getTargetId();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() throws IOException {
        NexusMetadataWriter.write(new NexusMetadataModel(
            getModId().get(),
            getDisplayName().getOrElse(getModId().get()),
            getModDescription().getOrElse(""),
            getModVersion().get(),
            getLicenseName().getOrElse("MIT"),
            getAuthors().getOrElse(java.util.List.of()),
            getLoader().get(),
            getMinecraftVersion().get(),
            getTargetId().get()
        ), getOutputDirectory().get().getAsFile().toPath());
    }
}
