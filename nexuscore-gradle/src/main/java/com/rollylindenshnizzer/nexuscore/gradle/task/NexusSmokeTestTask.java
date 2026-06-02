package com.rollylindenshnizzer.nexuscore.gradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@DisableCachingByDefault(because = "Runs nested Gradle builds and optional Minecraft launch processes.")
public abstract class NexusSmokeTestTask extends DefaultTask {
    @Inject
    public NexusSmokeTestTask() {
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getTargetDirectory();

    @Input
    public abstract Property<String> getTargetId();

    @Input
    public abstract Property<String> getSide();

    @Input
    @Optional
    public abstract Property<Boolean> getLaunchEnabled();

    @Input
    @Optional
    public abstract Property<Integer> getTimeoutSeconds();

    @Input
    @Optional
    public abstract Property<String> getRepositoryUrl();

    @Input
    @Optional
    public abstract ListProperty<String> getRequiredOutputMarkers();

    @TaskAction
    public void runSmokeTest() throws IOException, InterruptedException {
        String side = getSide().get().toLowerCase(Locale.ROOT);
        boolean launchTask = side.equals("client") || side.equals("server") || side.equals("datagen");
        if (launchTask && !shouldLaunch()) {
            getLogger().lifecycle("Skipping {} {} smoke launch. Re-run with -Pnexuscore.launchSmokeTests=true to launch Minecraft.", getTargetId().get(), getSide().get());
            return;
        }
        String taskName = switch (side) {
            case "client" -> "runClient";
            case "server" -> "runServer";
            case "datagen" -> "runData";
            case "build" -> "build";
            case "check" -> "check";
            default -> throw new GradleException("Unsupported NexusCore smoke side: " + getSide().get());
        };
        File targetDirectory = getTargetDirectory().get().getAsFile();
        if ("server".equals(side)) {
            acceptGeneratedServerEula(targetDirectory);
        }
        List<String> command = new ArrayList<>();
        command.add(gradleExecutable());
        command.add("-p");
        command.add(targetDirectory.getAbsolutePath());
        command.add(taskName);
        command.add("--stacktrace");
        command.add("--no-daemon");
        command.add("--console=plain");
        String repositoryUrl = getRepositoryUrl().getOrElse("");
        if (!repositoryUrl.isBlank()) {
            command.add("-Pnexuscore.mavenUrl=" + repositoryUrl);
        }
        getLogger().lifecycle("Running NexusCore {} {} command: {}", getTargetId().get(), getSide().get(), command);
        File logFile = smokeLogFile();
        Files.createDirectories(logFile.toPath().getParent());
        Files.deleteIfExists(logFile.toPath());
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(getProject().getRootProject().getProjectDir());
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.to(logFile));
        Process process = builder.start();
        int timeout = getTimeoutSeconds().getOrElse(launchTask ? 90 : 600);
        boolean completed = process.waitFor(timeout, TimeUnit.SECONDS);
        if (!completed) {
            destroyProcessTree(process);
            if (!launchTask) {
                throw new GradleException("NexusCore " + getTargetId().get() + " " + getSide().get() + " did not finish within " + Duration.ofSeconds(timeout) + ".");
            }
            assertRequiredMarkers(logFile);
            getLogger().lifecycle("NexusCore {} {} smoke launch stayed alive for {}; treating launch as successful.", getTargetId().get(), getSide().get(), Duration.ofSeconds(timeout));
            getLogger().lifecycle("NexusCore {} {} smoke log: {}", getTargetId().get(), getSide().get(), logFile.getAbsolutePath());
            return;
        }
        int exit = process.exitValue();
        if (exit != 0) {
            throw new GradleException("NexusCore " + getTargetId().get() + " " + getSide().get() + " smoke launch failed with exit code " + exit + ". Log: " + logFile.getAbsolutePath());
        }
        assertRequiredMarkers(logFile);
        getLogger().lifecycle("NexusCore {} {} smoke log: {}", getTargetId().get(), getSide().get(), logFile.getAbsolutePath());
    }

    private boolean shouldLaunch() {
        if (getLaunchEnabled().getOrElse(false)) {
            return true;
        }
        return wasRequested(getName()) || wasRequested("smokeTestAllTargets");
    }

    private boolean wasRequested(String taskName) {
        for (String requested : getProject().getGradle().getStartParameter().getTaskNames()) {
            if (requested.equals(taskName)
                || requested.equals(":" + taskName)
                || requested.endsWith(":" + taskName)) {
                return true;
            }
        }
        return false;
    }

    private void destroyProcessTree(Process process) throws InterruptedException {
        List<ProcessHandle> descendants = process.toHandle().descendants().toList();
        for (int i = descendants.size() - 1; i >= 0; i--) {
            descendants.get(i).destroyForcibly();
        }
        process.destroyForcibly();
        process.waitFor(10, TimeUnit.SECONDS);
    }

    private File smokeLogFile() {
        String fileName = getTargetId().get() + "-" + getSide().get().toLowerCase(Locale.ROOT) + ".log";
        return getProject().getLayout().getBuildDirectory().dir("nexuscore/smoke").get().file(fileName).getAsFile();
    }

    private void acceptGeneratedServerEula(File targetDirectory) throws IOException {
        String eula = "# Accepted by NexusCore smoke tests for local generated development server runs.\n"
            + "eula=true\n";
        for (Path path : List.of(
            targetDirectory.toPath().resolve("eula.txt"),
            targetDirectory.toPath().resolve("run/eula.txt"),
            targetDirectory.toPath().resolve("run/server/eula.txt")
        )) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, eula, StandardCharsets.UTF_8);
        }
    }

    private void assertRequiredMarkers(File logFile) throws IOException {
        List<String> markers = getRequiredOutputMarkers().getOrElse(List.of()).stream()
            .filter(marker -> marker != null && !marker.isBlank())
            .toList();
        if (markers.isEmpty()) {
            return;
        }
        String output = Files.isRegularFile(logFile.toPath()) ? Files.readString(logFile.toPath(), StandardCharsets.UTF_8) : "";
        for (String marker : markers) {
            if (!output.contains(marker)) {
                throw new GradleException("NexusCore " + getTargetId().get() + " " + getSide().get() + " smoke launch did not emit required runtime assertion marker '" + marker + "'. Log: " + logFile.getAbsolutePath());
            }
        }
    }

    private String gradleExecutable() {
        boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
        String wrapperName = windows ? "gradlew.bat" : "gradlew";
        File targetWrapper = new File(getTargetDirectory().get().getAsFile(), wrapperName);
        if (targetWrapper.isFile()) {
            return targetWrapper.getAbsolutePath();
        }
        File directory = getProject().getRootProject().getProjectDir();
        while (directory != null) {
            File wrapper = new File(directory, wrapperName);
            if (wrapper.isFile()) {
                return wrapper.getAbsolutePath();
            }
            directory = directory.getParentFile();
        }
        return windows ? "gradle.bat" : "gradle";
    }
}
