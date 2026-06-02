package com.rollylindenshnizzer.nexuscore.gradle;

import com.rollylindenshnizzer.nexuscore.gradle.target.NexusSupportedTargets;
import com.rollylindenshnizzer.nexuscore.gradle.target.NexusTarget;
import com.rollylindenshnizzer.nexuscore.gradle.target.NexusLoaderCoordinates;
import com.rollylindenshnizzer.nexuscore.gradle.task.GenerateNexusMetadataTask;
import com.rollylindenshnizzer.nexuscore.gradle.task.GenerateNexusTargetBuildTask;
import com.rollylindenshnizzer.nexuscore.gradle.task.NexusSmokeTestTask;
import com.rollylindenshnizzer.nexuscore.gradle.task.ValidateNexusTargetsTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Zip;

import java.util.ArrayList;
import java.util.List;

public final class NexusCoreGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);

        NexusCoreExtension extension = project.getExtensions().create("nexuscore", NexusCoreExtension.class);

        project.afterEvaluate(ignored -> configure(project, extension));
    }

    private void configure(Project project, NexusCoreExtension extension) {
        List<NexusTarget> targets = extension.getTargets().isEmpty()
            ? NexusSupportedTargets.all()
            : extension.getTargets().resolveTargets();

        targets.forEach(NexusSupportedTargets::validate);
        configureSourceSets(project, targets);

        List<String> targetIds = targets.stream().map(NexusTarget::targetId).toList();
        TaskProvider<ValidateNexusTargetsTask> validate = project.getTasks().register("validateNexusTargets", ValidateNexusTargetsTask.class, task -> {
            task.setGroup("nexuscore");
            task.setDescription("Validates the configured NexusCore target matrix.");
            task.getTargetIds().set(targetIds);
        });

        List<TaskProvider<GenerateNexusMetadataTask>> metadataTasks = new ArrayList<>();
        List<TaskProvider<GenerateNexusTargetBuildTask>> targetBuildTasks = new ArrayList<>();
        for (NexusTarget target : targets) {
            TaskProvider<GenerateNexusMetadataTask> metadata = metadataTask(project, extension, target);
            TaskProvider<GenerateNexusTargetBuildTask> targetBuild = targetBuildTask(project, extension, target);
            metadataTasks.add(metadata);
            targetBuildTasks.add(targetBuild);
            targetTasks(project, target, validate, metadata, targetBuild);
        }

        project.getTasks().register("generateNexusMetadata", task -> {
            task.setGroup("nexuscore");
            task.setDescription("Generates loader metadata for every configured NexusCore target.");
            metadataTasks.forEach(task::dependsOn);
        });

        project.getTasks().register("validateNexusMetadata", task -> {
            task.setGroup("verification");
            task.setDescription("Validates generated metadata files for every configured NexusCore target.");
            task.dependsOn("generateNexusMetadata");
            task.doLast(action -> {
                for (NexusTarget target : targets) {
                    java.io.File resources = project.getLayout().getBuildDirectory().dir("generated/nexuscore/" + target.targetId() + "/resources").get().getAsFile();
                    java.util.List<String> required = new java.util.ArrayList<>();
                    required.add("pack.mcmeta");
                    required.add("mixins." + extension.getModId().get() + ".json");
                    switch (target.loader()) {
                        case "fabric" -> required.add("fabric.mod.json");
                        case "quilt" -> required.add("quilt.mod.json");
                        case "forge" -> required.add("META-INF/mods.toml");
                        case "neoforge" -> required.add("META-INF/neoforge.mods.toml");
                        default -> throw new IllegalArgumentException("Unsupported loader: " + target.loader());
                    }
                    for (String path : required) {
                        java.io.File file = new java.io.File(resources, path);
                        if (!file.isFile() || file.length() == 0L) {
                            throw new org.gradle.api.GradleException("NexusCore metadata validation failed for " + target.targetId() + ": missing " + path);
                        }
                    }
                }
            });
        });

        project.getTasks().register("generateNexusTargetProjects", task -> {
            task.setGroup("nexuscore");
            task.setDescription("Generates standalone loader-backed Gradle projects for every configured NexusCore target.");
            targetBuildTasks.forEach(task::dependsOn);
        });

        project.getTasks().register("buildAllTargets", task -> {
            task.setGroup("nexuscore");
            task.setDescription("Builds all configured NexusCore targets.");
            for (NexusTarget target : targets) {
                task.dependsOn("build" + target.taskSuffix());
            }
        });

        project.getTasks().register("checkAllTargets", task -> {
            task.setGroup("nexuscore");
            task.setDescription("Checks all configured NexusCore targets.");
            for (NexusTarget target : targets) {
                task.dependsOn("check" + target.taskSuffix());
            }
            task.dependsOn("validateNexusMetadata");
        });

        project.getTasks().register("runDatagenAllTargets", task -> {
            task.setGroup("nexuscore");
            task.setDescription("Runs datagen for all configured NexusCore targets.");
            for (NexusTarget target : targets) {
                task.dependsOn("run" + target.taskSuffix() + "Datagen");
            }
        });

        project.getTasks().register("publishAllTargets", task -> {
            task.setGroup("nexuscore");
            task.setDescription("Publishes all configured NexusCore targets when publication is configured.");
            task.dependsOn("releaseNexusCore");
            task.doLast(action -> project.getLogger().lifecycle("NexusCore target release bundle is ready. Configure repository credentials or run generated target publish tasks for remote publishing."));
        });

        project.getTasks().register("smokeTestAllTargets", task -> {
            task.setGroup("nexuscore");
            task.setDescription("Launches generated client and server smoke tests for every configured NexusCore target when -Pnexuscore.launchSmokeTests=true is set.");
            for (NexusTarget target : targets) {
                task.dependsOn("run" + target.taskSuffix() + "Client");
                task.dependsOn("run" + target.taskSuffix() + "Server");
            }
        });

        project.getTasks().register("releaseNexusCore", Zip.class, task -> {
            task.setGroup("publishing");
            task.setDescription("Packages NexusCore generated metadata, target project builds, docs, and templates for release.");
            task.getArchiveBaseName().set("nexuscore-release");
            task.getArchiveVersion().set(extension.getVersion());
            task.from(project.getLayout().getBuildDirectory().dir("generated/nexuscore"));
            task.from(project.file("docs")).into("docs");
            task.from(project.file("templates")).into("templates");
            task.dependsOn("generateNexusMetadata", "generateNexusTargetProjects");
        });
    }

    private void configureSourceSets(Project project, List<NexusTarget> targets) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        ensure(sourceSets, "common", "common/src/main/java", "common/src/main/resources");

        for (NexusTarget target : targets) {
            String versionSourceSet = NexusSupportedTargets.versionSourceSet(target.minecraftVersion());
            ensure(sourceSets, versionSourceSet, versionSourceSet + "/src/main/java", versionSourceSet + "/src/main/resources");
            ensure(sourceSets, target.loader(), target.loader() + "/src/main/java", target.loader() + "/src/main/resources");
            SourceSet targetSourceSet = ensure(sourceSets, target.sourceSetName(), target.sourceSetName() + "/src/main/java", target.sourceSetName() + "/src/main/resources");
            targetSourceSet.getJava().srcDir("common/src/main/java");
            targetSourceSet.getJava().srcDir(versionSourceSet + "/src/main/java");
            targetSourceSet.getJava().srcDir(target.loader() + "/src/main/java");
            targetSourceSet.getResources().srcDir("common/src/main/resources");
            targetSourceSet.getResources().srcDir(versionSourceSet + "/src/main/resources");
            targetSourceSet.getResources().srcDir(target.loader() + "/src/main/resources");
        }
    }

    private SourceSet ensure(SourceSetContainer sourceSets, String name, String javaDir, String resourcesDir) {
        SourceSet existing = sourceSets.findByName(name);
        SourceSet sourceSet = existing == null ? sourceSets.create(name) : existing;
        sourceSet.getJava().srcDir(javaDir);
        sourceSet.getResources().srcDir(resourcesDir);
        return sourceSet;
    }

    private TaskProvider<GenerateNexusMetadataTask> metadataTask(Project project, NexusCoreExtension extension, NexusTarget target) {
        String name = "generate" + target.taskSuffix() + "Metadata";
        return project.getTasks().register(name, GenerateNexusMetadataTask.class, task -> {
            task.setGroup("nexuscore");
            task.setDescription("Generates " + target.targetId() + " metadata.");
            task.getModId().set(extension.getModId());
            task.getDisplayName().set(extension.getDisplayName());
            task.getModDescription().set(extension.getDescription());
            task.getModVersion().set(extension.getVersion());
            task.getLicenseName().set(extension.getLicense());
            task.getAuthors().set(extension.getAuthors());
            task.getLoader().set(target.loader());
            task.getMinecraftVersion().set(target.minecraftVersion());
            task.getTargetId().set(target.targetId());
            task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("generated/nexuscore/" + target.targetId() + "/resources"));
        });
    }

    private TaskProvider<GenerateNexusTargetBuildTask> targetBuildTask(Project project, NexusCoreExtension extension, NexusTarget target) {
        String name = "generate" + target.taskSuffix() + "TargetProject";
        return project.getTasks().register(name, GenerateNexusTargetBuildTask.class, task -> {
            task.setGroup("nexuscore");
            task.setDescription("Generates the standalone " + target.targetId() + " loader project.");
            task.getModId().set(extension.getModId());
            task.getGroupName().set(extension.getGroup().orElse(project.getGroup().toString()));
            task.getModVersion().set(extension.getVersion());
            task.getLoader().set(target.loader());
            task.getMinecraftVersion().set(target.minecraftVersion());
            task.getTargetId().set(target.targetId());
            task.getLoomVersion().set(NexusLoaderCoordinates.loomVersion(project, target.minecraftVersion()));
            task.getQuiltLoomVersion().set(NexusLoaderCoordinates.quiltLoomVersion(project));
            task.getForgeGradleVersion().set(NexusLoaderCoordinates.forgeGradleVersion(project));
            task.getModDevGradleVersion().set(NexusLoaderCoordinates.modDevGradleVersion(project));
            task.getFabricLoaderVersion().set(NexusLoaderCoordinates.fabricLoader(project, target.minecraftVersion()));
            task.getFabricApiVersion().set(NexusLoaderCoordinates.fabricApi(project, target.minecraftVersion()));
            task.getQuiltLoaderVersion().set(NexusLoaderCoordinates.quiltLoader(project));
            task.getQuiltedFabricApiVersion().set(NexusLoaderCoordinates.quiltedFabricApi(project, target.minecraftVersion()));
            task.getForgeVersion().set(NexusLoaderCoordinates.forgeVersion(project, target.minecraftVersion()));
            task.getNeoForgeVersion().set(NexusLoaderCoordinates.neoForgeVersion(project, target.minecraftVersion()));
            task.getNexusCoreVersion().set(NexusLoaderCoordinates.nexusCoreVersion(project));
            task.getUserEntrypointClass().set(extension.getEntrypointClass());
            task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("generated/nexuscore/targets/" + target.targetId()));
        });
    }

    private void targetTasks(Project project, NexusTarget target, TaskProvider<ValidateNexusTargetsTask> validate, TaskProvider<GenerateNexusMetadataTask> metadata, TaskProvider<GenerateNexusTargetBuildTask> targetBuild) {
        project.getTasks().register("run" + target.taskSuffix() + "Client", NexusSmokeTestTask.class, task -> runTask(task, target, "client", validate, metadata, targetBuild));
        project.getTasks().register("run" + target.taskSuffix() + "Server", NexusSmokeTestTask.class, task -> runTask(task, target, "server", validate, metadata, targetBuild));
        project.getTasks().register("run" + target.taskSuffix() + "Datagen", NexusSmokeTestTask.class, task -> runTask(task, target, "datagen", validate, metadata, targetBuild));
        project.getTasks().register("build" + target.taskSuffix(), NexusSmokeTestTask.class, task -> runTask(task, target, "build", validate, metadata, targetBuild));
        project.getTasks().register("check" + target.taskSuffix(), NexusSmokeTestTask.class, task -> runTask(task, target, "check", validate, metadata, targetBuild));
    }

    private void runTask(NexusSmokeTestTask task, NexusTarget target, String side, TaskProvider<ValidateNexusTargetsTask> validate, TaskProvider<GenerateNexusMetadataTask> metadata, TaskProvider<GenerateNexusTargetBuildTask> targetBuild) {
        task.setGroup("nexuscore");
        task.setDescription("Prepares a " + side + " run for " + target.targetId() + ".");
        task.dependsOn(validate, metadata, targetBuild);
        task.getTargetId().set(target.targetId());
        task.getSide().set(side);
        task.getTargetDirectory().set(task.getProject().getLayout().getBuildDirectory().dir("generated/nexuscore/targets/" + target.targetId()));
        task.getLaunchEnabled().set(Boolean.parseBoolean(String.valueOf(task.getProject().findProperty("nexuscore.launchSmokeTests"))));
        Object timeout = task.getProject().findProperty("nexuscore.smokeTimeoutSeconds");
        if (timeout != null) {
            task.getTimeoutSeconds().set(Integer.parseInt(String.valueOf(timeout)));
        } else {
            task.getTimeoutSeconds().set(("build".equals(side) || "check".equals(side)) ? 900 : 90);
        }
        Object repositoryUrl = task.getProject().findProperty("nexuscore.mavenUrl");
        if (repositoryUrl != null && !repositoryUrl.toString().isBlank()) {
            task.getRepositoryUrl().set(repositoryUrl.toString());
        } else {
            String environmentRepositoryUrl = System.getenv("NEXUSCORE_MAVEN_URL");
            if (environmentRepositoryUrl != null && !environmentRepositoryUrl.isBlank()) {
                task.getRepositoryUrl().set(environmentRepositoryUrl);
            }
        }
        String sideProperty = "client".equals(side) ? "nexuscore.requiredClientSmokeMarkers" : "server".equals(side) ? "nexuscore.requiredServerSmokeMarkers" : "nexuscore.requiredSmokeMarkers";
        String sideEnvironment = "client".equals(side) ? "NEXUSCORE_REQUIRED_CLIENT_SMOKE_MARKERS" : "server".equals(side) ? "NEXUSCORE_REQUIRED_SERVER_SMOKE_MARKERS" : "NEXUSCORE_REQUIRED_SMOKE_MARKERS";
        Object markers = task.getProject().findProperty(sideProperty);
        if (markers == null) {
            markers = System.getenv(sideEnvironment);
        }
        if (markers == null) {
            markers = task.getProject().findProperty("nexuscore.requiredSmokeMarkers");
        }
        if (markers == null) {
            markers = System.getenv("NEXUSCORE_REQUIRED_SMOKE_MARKERS");
        }
        if (markers != null && !markers.toString().isBlank() && ("client".equals(side) || "server".equals(side))) {
            task.getRequiredOutputMarkers().set(
                java.util.Arrays.stream(markers.toString().split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .toList()
            );
        }
    }
}
