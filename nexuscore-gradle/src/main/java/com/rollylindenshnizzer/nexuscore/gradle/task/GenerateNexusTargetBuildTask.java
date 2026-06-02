package com.rollylindenshnizzer.nexuscore.gradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DisableCachingByDefault(because = "Generates a mutable Gradle target project including wrapper files for local execution.")
public abstract class GenerateNexusTargetBuildTask extends DefaultTask {
    @Inject
    public GenerateNexusTargetBuildTask() {
    }

    @Input
    public abstract Property<String> getModId();

    @Input
    public abstract Property<String> getGroupName();

    @Input
    public abstract Property<String> getModVersion();

    @Input
    public abstract Property<String> getLoader();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getTargetId();

    @Input
    public abstract Property<String> getLoomVersion();

    @Input
    public abstract Property<String> getQuiltLoomVersion();

    @Input
    public abstract Property<String> getForgeGradleVersion();

    @Input
    public abstract Property<String> getModDevGradleVersion();

    @Input
    public abstract Property<String> getFabricLoaderVersion();

    @Input
    public abstract Property<String> getFabricApiVersion();

    @Input
    public abstract Property<String> getQuiltLoaderVersion();

    @Input
    public abstract Property<String> getQuiltedFabricApiVersion();

    @Input
    public abstract Property<String> getForgeVersion();

    @Input
    public abstract Property<String> getNeoForgeVersion();

    @Input
    public abstract Property<String> getNexusCoreVersion();

    @Input
    public abstract Property<String> getUserEntrypointClass();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() throws IOException {
        Path output = getOutputDirectory().get().getAsFile().toPath();
        Files.createDirectories(output);
        Files.writeString(output.resolve("settings.gradle"), settings());
        Files.writeString(output.resolve("build.gradle"), buildFile());
        Files.writeString(output.resolve("gradle.properties"), gradleProperties());
        Files.writeString(output.resolve("README.md"), readme());
        writeGradleWrapper(output);
        writeGeneratedSource(output);
    }

    private String settings() {
        return """
            pluginManagement {
                repositories {
                    def propertyValue = gradle.startParameter.projectProperties['nexuscore.mavenUrl']
                    def systemValue = System.getProperty('nexuscore.mavenUrl')
                    def environmentValue = System.getenv('NEXUSCORE_MAVEN_URL')
                    def fileValue = null
                    def propertiesFile = file('gradle.properties')
                    if (propertiesFile.isFile()) {
                        def properties = new java.util.Properties()
                        propertiesFile.withInputStream { properties.load(it) }
                        fileValue = properties.getProperty('nexuscore.mavenUrl')
                    }
                def nexusCoreMaven = [propertyValue, systemValue, environmentValue, fileValue].find { it != null && !it.isBlank() }
                if (nexusCoreMaven != null && !nexusCoreMaven.isBlank()) {
                    maven { url = nexusCoreMaven.startsWith('file:') ? new URI(nexusCoreMaven.replace(' ', '%%20')) : uri(nexusCoreMaven) }
                }
                    gradlePluginPortal()
                    mavenLocal()
                    maven { url = uri('../../../../repo') }
                    maven { url = 'https://maven.fabricmc.net/' }
                    maven { url = 'https://maven.quiltmc.org/repository/release/' }
                    maven { url = 'https://maven.minecraftforge.net/' }
                    maven { url = 'https://maven.neoforged.net/releases' }
                    mavenCentral()
                }
            }

            dependencyResolutionManagement {
                repositories {
                    def propertyValue = gradle.startParameter.projectProperties['nexuscore.mavenUrl']
                    def systemValue = System.getProperty('nexuscore.mavenUrl')
                    def environmentValue = System.getenv('NEXUSCORE_MAVEN_URL')
                    def fileValue = null
                    def propertiesFile = file('gradle.properties')
                    if (propertiesFile.isFile()) {
                        def properties = new java.util.Properties()
                        propertiesFile.withInputStream { properties.load(it) }
                        fileValue = properties.getProperty('nexuscore.mavenUrl')
                    }
                def nexusCoreMaven = [propertyValue, systemValue, environmentValue, fileValue].find { it != null && !it.isBlank() }
                if (nexusCoreMaven != null && !nexusCoreMaven.isBlank()) {
                    maven { url = nexusCoreMaven.startsWith('file:') ? new URI(nexusCoreMaven.replace(' ', '%%20')) : uri(nexusCoreMaven) }
                }
                    mavenLocal()
                    maven { url = uri('../../../../repo') }
                    maven { url = 'https://maven.fabricmc.net/' }
                    maven { url = 'https://maven.quiltmc.org/repository/release/' }
                    maven { url = 'https://maven.minecraftforge.net/' }
                    maven { url = 'https://maven.neoforged.net/releases' }
                    mavenCentral()
                }
            }

            rootProject.name = '%s'
            """.formatted(getTargetId().get());
    }

    private String buildFile() {
        return switch (getLoader().get()) {
            case "fabric" -> loomBuild(fabricLoomPluginId(), getLoomVersion().get(), "net.fabricmc:fabric-loader:" + getFabricLoaderVersion().get(), "net.fabricmc.fabric-api:fabric-api:" + getFabricApiVersion().get());
            case "quilt" -> loomBuild("org.quiltmc.loom", getQuiltLoomVersion().get(), "org.quiltmc:quilt-loader:" + getQuiltLoaderVersion().get(), "org.quiltmc.quilted-fabric-api:quilted-fabric-api:" + getQuiltedFabricApiVersion().get());
            case "forge" -> forgeBuild();
            case "neoforge" -> neoForgeBuild();
            default -> throw new IllegalArgumentException("Unsupported loader: " + getLoader().get());
        };
    }

    private String fabricLoomPluginId() {
        return "26.1.2".equals(getMinecraftVersion().get()) ? "net.fabricmc.fabric-loom" : "fabric-loom";
    }

    private String loomBuild(String pluginId, String pluginVersion, String loaderDependency, String apiDependency) {
        return """
            plugins {
                id '%s' version '%s'
                id 'maven-publish'
            }

            %s

            group = '%s'
            version = '%s'

            base {
                archivesName = '%s'
            }

            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(%d)
                }
                withSourcesJar()
            }

            dependencies {
                minecraft 'com.mojang:minecraft:%s'
                %s
                %s '%s'
                %s '%s'
                implementation('com.rollylindenshnizzer.nexuscore:nexuscore-api:%s') { changing = true }
                implementation('com.rollylindenshnizzer.nexuscore:nexuscore-core:%s') { changing = true }
                implementation('com.rollylindenshnizzer.nexuscore.adapters:common:%s') { changing = true }
                implementation('com.rollylindenshnizzer.nexuscore.adapters:%s:%s') { changing = true }
%s
            }

            sourceSets {
                main {
                    java.srcDirs = %s
                    resources.srcDirs = %s
                }
            }

            loom {
                mods {
                    '%s' {
                        sourceSet sourceSets.main
                    }
                }
%s
            }

%s

            processResources {
                inputs.property 'version', project.version
                filesMatching(['fabric.mod.json', 'quilt.mod.json', 'META-INF/mods.toml', 'META-INF/neoforge.mods.toml']) {
                    expand 'version': project.version
                }
            }

            publishing {
                publications {
                    mavenJava(MavenPublication) {
                        from components.java
                    }
                }
                repositories {
                    maven {
                        name = 'nexuscoreLocal'
                        url = layout.buildDirectory.dir('repo')
                    }
                }
            }
            """.formatted(
                pluginId,
                pluginVersion,
                repositoriesBlock(),
                getGroupName().get(),
                getModVersion().get(),
                getModId().get() + "-" + getTargetId().get(),
                javaLanguageVersion(),
                getMinecraftVersion().get(),
                mappingsDependency(),
                loaderApiConfiguration(),
                loaderDependency,
                loaderApiConfiguration(),
                apiDependency,
                getNexusCoreVersion().get(),
                getNexusCoreVersion().get(),
                getNexusCoreVersion().get(),
                getTargetId().get(),
                getNexusCoreVersion().get(),
                loomExtraDependencies(),
                javaDirs(),
                resourceDirs(),
                getModId().get(),
                loomRunConfiguration(),
                loomRuntimeConfiguration()
            );
    }

    private String loomRunConfiguration() {
        if (!"quilt".equals(getLoader().get())) {
            return "";
        }
        return """
                runs {
                    client {
                        client()
                        setConfigName('NexusCore Client')
                        ideConfigGenerated = true
                        runDir 'run/client'
                    }
                    server {
                        server()
                        setConfigName('NexusCore Server')
                        ideConfigGenerated = true
                        runDir 'run/server'
                    }
                }
            """;
    }

    private String loomRuntimeConfiguration() {
        return "";
    }

    private String loomExtraDependencies() {
        String version = getNexusCoreVersion().get();
        StringBuilder dependencies = new StringBuilder()
            .append("                include('com.rollylindenshnizzer.nexuscore:nexuscore-api:").append(version).append("') { changing = true }\n")
            .append("                include('com.rollylindenshnizzer.nexuscore:nexuscore-core:").append(version).append("') { changing = true }\n")
            .append("                include('com.rollylindenshnizzer.nexuscore.adapters:common:").append(version).append("') { changing = true }\n")
            .append("                include('com.rollylindenshnizzer.nexuscore.adapters:").append(getTargetId().get()).append(":").append(version).append("') { changing = true }");
        if ("quilt".equals(getLoader().get())) {
            dependencies.append("\n")
                .append("                runtimeOnly 'org.ow2.asm:asm:9.9.1'\n")
                .append("                runtimeOnly 'org.ow2.asm:asm-analysis:9.9.1'\n")
                .append("                runtimeOnly 'org.ow2.asm:asm-commons:9.9.1'\n")
                .append("                runtimeOnly 'org.ow2.asm:asm-tree:9.9.1'\n")
                .append("                runtimeOnly 'org.ow2.asm:asm-util:9.9.1'\n")
                .append("                runtimeOnly 'net.fabricmc:sponge-mixin:0.15.5+mixin.0.8.7'\n")
                .append("                runtimeOnly 'org.quiltmc:quilt-json5:1.0.4+final'\n")
                .append("                runtimeOnly 'org.quiltmc:quilt-config:1.3.3'");
        }
        return dependencies.toString();
    }

    private String mappingsDependency() {
        return "26.1.2".equals(getMinecraftVersion().get()) ? "" : "mappings loom.officialMojangMappings()";
    }

    private String loaderApiConfiguration() {
        return "26.1.2".equals(getMinecraftVersion().get()) ? "implementation" : "modImplementation";
    }

    private String forgeBuild() {
        return """
            plugins {
                id 'net.minecraftforge.gradle' version '%s'
                id 'maven-publish'
            }

            %s

            group = '%s'
            version = '%s'

            base {
                archivesName = '%s'
            }

            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(17)
                }
                withSourcesJar()
            }

            minecraft {
                mappings channel: 'official', version: '%s'
                runs {
                    client {
                        workingDirectory project.file('run/client')
                        mods { '%s' { source sourceSets.main } }
                    }
                    server {
                        workingDirectory project.file('run/server')
                        args '--nogui'
                        mods { '%s' { source sourceSets.main } }
                    }
                    data {
                        workingDirectory project.file('run/datagen')
                        args '--mod', '%s', '--all', '--output', file('src/generated/resources/'), '--existing', file('src/main/resources/')
                        mods { '%s' { source sourceSets.main } }
                    }
                }
            }

            dependencies {
                minecraft 'net.minecraftforge:forge:%s'
                implementation('com.rollylindenshnizzer.nexuscore:nexuscore-api:%s') { changing = true }
                implementation('com.rollylindenshnizzer.nexuscore:nexuscore-core:%s') { changing = true }
                implementation('com.rollylindenshnizzer.nexuscore.adapters:common:%s') { changing = true }
                implementation('com.rollylindenshnizzer.nexuscore.adapters:%s:%s') { changing = true }
                minecraftLibrary('com.rollylindenshnizzer.nexuscore:nexuscore-api:%s') { changing = true }
                minecraftLibrary('com.rollylindenshnizzer.nexuscore:nexuscore-core:%s') { changing = true }
                minecraftLibrary('com.rollylindenshnizzer.nexuscore.adapters:common:%s') { changing = true }
                minecraftLibrary('com.rollylindenshnizzer.nexuscore.adapters:%s:%s') { changing = true }
            }

            sourceSets {
                main {
                    java.srcDirs = %s
                    resources.srcDirs = %s
                }
            }

            publishing {
                publications {
                    mavenJava(MavenPublication) {
                        from components.java
                    }
                }
                repositories {
                    maven {
                        name = 'nexuscoreLocal'
                        url = layout.buildDirectory.dir('repo')
                    }
                }
            }
            """.formatted(
                getForgeGradleVersion().get(),
                repositoriesBlock(),
                getGroupName().get(),
                getModVersion().get(),
                getModId().get() + "-" + getTargetId().get(),
                getMinecraftVersion().get(),
                getModId().get(),
                getModId().get(),
                getModId().get(),
                getModId().get(),
                getForgeVersion().get(),
                getNexusCoreVersion().get(),
                getNexusCoreVersion().get(),
                getNexusCoreVersion().get(),
                getTargetId().get(),
                getNexusCoreVersion().get(),
                getNexusCoreVersion().get(),
                getNexusCoreVersion().get(),
                getNexusCoreVersion().get(),
                getTargetId().get(),
                getNexusCoreVersion().get(),
                javaDirs(),
                resourceDirs()
            );
    }

    private String neoForgeBuild() {
        return """
            plugins {
                id 'net.neoforged.moddev' version '%s'
                id 'maven-publish'
            }

            %s

            group = '%s'
            version = '%s'

            base {
                archivesName = '%s'
            }

            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(%d)
                }
                withSourcesJar()
            }

            neoForge {
                version = '%s'
                mods {
                    '%s' {
                        sourceSet sourceSets.main
                    }
                }
                runs {
                    client {
                        client()
                    }
                    server {
                        server()
                        programArgument '--nogui'
                    }
                    data {
                        data()
                        programArguments.addAll '--mod', '%s', '--all', '--output', file('src/generated/resources/').absolutePath, '--existing', file('src/main/resources/').absolutePath
                    }
                }
            }

            sourceSets {
                main {
                    java.srcDirs = %s
                    resources.srcDirs = %s
                }
            }

            dependencies {
%s
            }

            publishing {
                publications {
                    mavenJava(MavenPublication) {
                        from components.java
                    }
                }
                repositories {
                    maven {
                        name = 'nexuscoreLocal'
                        url = layout.buildDirectory.dir('repo')
                    }
                }
            }
            """.formatted(
                getModDevGradleVersion().get(),
                repositoriesBlock(),
                getGroupName().get(),
                getModVersion().get(),
                getModId().get() + "-" + getTargetId().get(),
                javaLanguageVersion(),
                getNeoForgeVersion().get(),
                getModId().get(),
                getModId().get(),
                javaDirs(),
                resourceDirs(),
                neoForgeNexusCoreDependencies()
            );
    }

    private String neoForgeNexusCoreDependencies() {
        List<String> coordinates = List.of(
            "com.rollylindenshnizzer.nexuscore:nexuscore-api:" + getNexusCoreVersion().get(),
            "com.rollylindenshnizzer.nexuscore:nexuscore-core:" + getNexusCoreVersion().get(),
            "com.rollylindenshnizzer.nexuscore.adapters:common:" + getNexusCoreVersion().get(),
            "com.rollylindenshnizzer.nexuscore.adapters:" + getTargetId().get() + ":" + getNexusCoreVersion().get()
        );
        StringBuilder dependencies = new StringBuilder();
        for (String coordinate : coordinates) {
            dependencies.append("                jarJar(implementation('")
                .append(coordinate)
                .append("') { changing = true }) {\n")
                .append("                    version { prefer '")
                .append(getNexusCoreVersion().get())
                .append("' }\n")
                .append("                }\n");
        }
        if (needsNeoForgeAdditionalRuntimeClasspath()) {
            for (String coordinate : coordinates) {
                dependencies.append("                additionalRuntimeClasspath('")
                    .append(coordinate)
                    .append("') { changing = true }\n");
            }
        }
        return dependencies.toString().stripTrailing();
    }

    private boolean needsNeoForgeAdditionalRuntimeClasspath() {
        String[] parts = getMinecraftVersion().get().split("\\.");
        if (parts.length < 3) {
            return false;
        }
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2].replaceAll("[^0-9].*", ""));
            return major < 1 || (major == 1 && (minor < 21 || (minor == 21 && patch <= 8)));
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private String repositoriesBlock() {
        return """
            repositories {
                def propertyValue = project.findProperty('nexuscore.mavenUrl')
                def systemValue = System.getProperty('nexuscore.mavenUrl')
                def environmentValue = System.getenv('NEXUSCORE_MAVEN_URL')
            def nexusCoreMaven = [propertyValue, systemValue, environmentValue].find { it != null && !it.toString().isBlank() }
            if (nexusCoreMaven != null && !nexusCoreMaven.toString().isBlank()) {
                def nexusCoreMavenText = nexusCoreMaven.toString()
                maven { url = nexusCoreMavenText.startsWith('file:') ? new URI(nexusCoreMavenText.replace(' ', '%20')) : uri(nexusCoreMavenText) }
            }
                mavenLocal()
                maven { url = uri('../../../../repo') }
                maven { url = 'https://maven.fabricmc.net/' }
                maven { url = 'https://maven.quiltmc.org/repository/release/' }
                maven { url = 'https://maven.minecraftforge.net/' }
                maven { url = 'https://maven.neoforged.net/releases' }
                mavenCentral()
            }

            configurations.configureEach {
                resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
            }
            """;
    }

    private String javaDirs() {
        return "['src/main/java', '" + source("common/src/main/java") + "', '" + source(versionFolder() + "/src/main/java") + "', '" + source(getLoader().get() + "/src/main/java") + "', '" + source(getTargetId().get() + "/src/main/java") + "']";
    }

    private String resourceDirs() {
        return "['src/main/resources', '" + generatedResources() + "', '" + source("common/src/main/resources") + "', '" + source(versionFolder() + "/src/main/resources") + "', '" + source(getLoader().get() + "/src/main/resources") + "', '" + source(getTargetId().get() + "/src/main/resources") + "']";
    }

    private String source(String relativePath) {
        return getProject().file(relativePath).getAbsolutePath().replace("\\", "\\\\");
    }

    private String versionFolder() {
        return "mc_" + getMinecraftVersion().get().replace('.', '_');
    }

    private String generatedResources() {
        return getProject().getLayout().getBuildDirectory().dir("generated/nexuscore/" + getTargetId().get() + "/resources").get().getAsFile().getAbsolutePath().replace("\\", "\\\\");
    }

    private int javaLanguageVersion() {
        return switch (getMinecraftVersion().get()) {
            case "26.1.2" -> 25;
            case "1.21.1" -> 21;
            default -> 17;
        };
    }

    private void writeGeneratedSource(Path output) throws IOException {
        Path packageDirectory = output.resolve("src/main/java").resolve(generatedPackage().replace('.', '/'));
        Files.createDirectories(packageDirectory);
        Files.writeString(packageDirectory.resolve("NexusGeneratedBootstrap.java"), generatedBootstrap());
        switch (getLoader().get()) {
            case "fabric" -> {
                Files.writeString(packageDirectory.resolve("FabricEntrypoint.java"), fabricEntrypoint());
                Files.writeString(packageDirectory.resolve("FabricClientEntrypoint.java"), fabricClientEntrypoint());
                Files.writeString(packageDirectory.resolve("FabricServerEntrypoint.java"), fabricServerEntrypoint());
            }
            case "quilt" -> {
                Files.writeString(packageDirectory.resolve("QuiltEntrypoint.java"), quiltEntrypoint());
                Files.writeString(packageDirectory.resolve("QuiltClientEntrypoint.java"), quiltClientEntrypoint());
                Files.writeString(packageDirectory.resolve("QuiltServerEntrypoint.java"), quiltServerEntrypoint());
            }
            case "forge" -> Files.writeString(packageDirectory.resolve("ForgeEntrypoint.java"), forgeEntrypoint());
            case "neoforge" -> Files.writeString(packageDirectory.resolve("NeoForgeEntrypoint.java"), neoForgeEntrypoint());
            default -> throw new IllegalArgumentException("Unsupported loader: " + getLoader().get());
        }
    }

    private String generatedPackage() {
        return "com.rollylindenshnizzer.nexuscore.generated." + getTargetId().get();
    }

    private String providerClass() {
        String version = switch (getMinecraftVersion().get()) {
            case "1.20.1" -> "1201";
            case "1.21.1" -> "1211";
            case "26.1.2" -> "2612";
            default -> throw new IllegalArgumentException("Unsupported Minecraft version: " + getMinecraftVersion().get());
        };
        String packageVersion = "mc" + version;
        return switch (getLoader().get()) {
            case "fabric" -> "com.rollylindenshnizzer.nexuscore.adapter.fabric." + packageVersion + ".Fabric" + version + "ServiceProvider";
            case "forge" -> "com.rollylindenshnizzer.nexuscore.adapter.forge." + packageVersion + ".Forge" + version + "ServiceProvider";
            case "neoforge" -> "com.rollylindenshnizzer.nexuscore.adapter.neoforge." + packageVersion + ".NeoForge" + version + "ServiceProvider";
            case "quilt" -> "com.rollylindenshnizzer.nexuscore.adapter.quilt." + packageVersion + ".Quilt" + version + "ServiceProvider";
            default -> throw new IllegalArgumentException("Unsupported loader: " + getLoader().get());
        };
    }

    private String generatedBootstrap() {
        String entrypoint = escapeJava(getUserEntrypointClass().get());
        return """
            package %s;

            import com.rollylindenshnizzer.nexuscore.core.adapter.NativeLoaderCommandBridge;
            import com.rollylindenshnizzer.nexuscore.core.adapter.NativeLoaderNetworkBridge;
            import com.rollylindenshnizzer.nexuscore.core.adapter.NativeMinecraftRegistryBridge;
            import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;
            import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
            import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

            final class NexusGeneratedBootstrap {
                private static boolean userInitialized;

                private NexusGeneratedBootstrap() {
                }

                static void init() {
                    withContext(() -> {
                        install();
                        invokeUserEntrypoint();
                        NexusCoreCommonBootstrap.init();
                    });
                }

                static void client(Object client) {
                    withContext(() -> {
                        install();
                        NexusServices.get().events().fireClientStarted(client);
                    });
                }

                static void server(Object server) {
                    withContext(() -> {
                        install();
                        NexusCoreCommonBootstrap.init();
                        NexusServices.get().events().fireServerStarted(server);
                    });
                }

                static void playerJoined(Object player, String name) {
                    withContext(() -> {
                        install();
                        NexusServices.get().events().firePlayerJoined(new NexusPlayer(player, name == null ? "player" : name));
                    });
                }

                static void serverTick(long tick) {
                    withContext(() -> {
                        install();
                        NexusServices.get().events().fireServerTick(tick);
                    });
                }

                static void clientTick(long tick) {
                    withContext(() -> {
                        install();
                        NexusServices.get().events().fireClientTick(tick);
                    });
                }

                static void commands(Object dispatcher) {
                    withContext(() -> {
                        install();
                        if (NexusServices.get().commands() instanceof NativeLoaderCommandBridge commandBridge) {
                            commandBridge.registerWithDispatcher(dispatcher);
                        }
                    });
                }

                static void registries(Object event) {
                    withContext(() -> {
                        install();
                        if (NexusServices.get().registries() instanceof NativeMinecraftRegistryBridge registryBridge) {
                            registryBridge.registerWithLoaderEvent(event);
                        }
                    });
                }

                static void networkPayloads(Object event) {
                    withContext(() -> {
                        install();
                        if (NexusServices.get().networking() instanceof NativeLoaderNetworkBridge networkBridge) {
                            networkBridge.registerWithPayloadEvent(event);
                        }
                    });
                }

                private static void withContext(Runnable action) {
                    Thread thread = Thread.currentThread();
                    ClassLoader previous = thread.getContextClassLoader();
                    ClassLoader loader = NexusGeneratedBootstrap.class.getClassLoader();
                    if (loader != null) {
                        thread.setContextClassLoader(loader);
                    }
                    try {
                        action.run();
                    } finally {
                        thread.setContextClassLoader(previous);
                    }
                }

                private static void install() {
                    if (!NexusServices.isInstalled()) {
                        NexusServices.install(new %s());
                    }
                }

                private static void invokeUserEntrypoint() {
                    if (userInitialized || "%s".isBlank()) {
                        return;
                    }
                    userInitialized = true;
                    try {
                        Class.forName("%s", true, NexusGeneratedBootstrap.class.getClassLoader()).getMethod("init").invoke(null);
                    } catch (ReflectiveOperationException exception) {
                        throw new IllegalStateException("NexusCore could not invoke configured entrypoint class '%s'. Expected a public static init() method.", exception);
                    }
                }
            }
            """.formatted(generatedPackage(), providerClass(), entrypoint, entrypoint, entrypoint);
    }

    private String fabricEntrypoint() {
        return """
            package %s;

            import net.fabricmc.api.ModInitializer;

            public final class FabricEntrypoint implements ModInitializer {
                @Override
                public void onInitialize() {
                    NexusGeneratedBootstrap.init();
                }
            }
            """.formatted(generatedPackage());
    }

    private String fabricClientEntrypoint() {
        return """
            package %s;

            import net.fabricmc.api.ClientModInitializer;

            public final class FabricClientEntrypoint implements ClientModInitializer {
                @Override
                public void onInitializeClient() {
                    NexusGeneratedBootstrap.init();
                    NexusGeneratedBootstrap.client(clientInstance());
                }

                private static Object clientInstance() {
                    for (String className : new String[]{"net.minecraft.client.Minecraft", "net.minecraft.client.MinecraftClient", "net.minecraft.class_310"}) {
                        try {
                            Class<?> type = Class.forName(className);
                            for (String methodName : new String[]{"getInstance", "method_1551"}) {
                                try {
                                    return type.getMethod(methodName).invoke(null);
                                } catch (ReflectiveOperationException ignored) {
                                }
                            }
                        } catch (ClassNotFoundException ignored) {
                        }
                    }
                    return null;
                }
            }
            """.formatted(generatedPackage());
    }

    private String fabricServerEntrypoint() {
        return """
            package %s;

            import net.fabricmc.api.DedicatedServerModInitializer;
            import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
            import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

            public final class FabricServerEntrypoint implements DedicatedServerModInitializer {
                private long serverTicks;

                @Override
                public void onInitializeServer() {
                    NexusGeneratedBootstrap.init();
                    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                        NexusGeneratedBootstrap.commands(dispatcher(server));
                        NexusGeneratedBootstrap.server(server);
                    });
                    ServerTickEvents.END_SERVER_TICK.register(server -> NexusGeneratedBootstrap.serverTick(++serverTicks));
                }

                private static Object dispatcher(Object server) {
                    try {
                        Object commands = server.getClass().getMethod("getCommands").invoke(server);
                        return commands.getClass().getMethod("getDispatcher").invoke(commands);
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
                        return null;
                    }
                }
            }
            """.formatted(generatedPackage());
    }

    private String quiltEntrypoint() {
        return """
            package %s;

            import net.fabricmc.api.ModInitializer;

            public final class QuiltEntrypoint implements ModInitializer {
                @Override
                public void onInitialize() {
                    NexusGeneratedBootstrap.init();
                }
            }
            """.formatted(generatedPackage());
    }

    private String quiltClientEntrypoint() {
        return """
            package %s;

            import net.fabricmc.api.ClientModInitializer;

            public final class QuiltClientEntrypoint implements ClientModInitializer {
                @Override
                public void onInitializeClient() {
                    NexusGeneratedBootstrap.init();
                    NexusGeneratedBootstrap.client(clientInstance());
                }

                private static Object clientInstance() {
                    for (String className : new String[]{"net.minecraft.client.Minecraft", "net.minecraft.client.MinecraftClient", "net.minecraft.class_310"}) {
                        try {
                            Class<?> type = Class.forName(className);
                            for (String methodName : new String[]{"getInstance", "method_1551"}) {
                                try {
                                    return type.getMethod(methodName).invoke(null);
                                } catch (ReflectiveOperationException ignored) {
                                }
                            }
                        } catch (ClassNotFoundException ignored) {
                        }
                    }
                    return null;
                }
            }
            """.formatted(generatedPackage());
    }

    private String quiltServerEntrypoint() {
        return """
            package %s;

            import net.fabricmc.api.DedicatedServerModInitializer;
            import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
            import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

            public final class QuiltServerEntrypoint implements DedicatedServerModInitializer {
                private long serverTicks;

                @Override
                public void onInitializeServer() {
                    NexusGeneratedBootstrap.init();
                    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                        NexusGeneratedBootstrap.commands(dispatcher(server));
                        NexusGeneratedBootstrap.server(server);
                    });
                    ServerTickEvents.END_SERVER_TICK.register(server -> NexusGeneratedBootstrap.serverTick(++serverTicks));
                }

                private static Object dispatcher(Object server) {
                    try {
                        Object commands = server.getClass().getMethod("getCommands").invoke(server);
                        return commands.getClass().getMethod("getDispatcher").invoke(commands);
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
                        return null;
                    }
                }
            }
            """.formatted(generatedPackage());
    }

    private String forgeEntrypoint() {
        return """
            package %s;

            import net.minecraftforge.common.MinecraftForge;
            import net.minecraftforge.event.RegisterCommandsEvent;
            import net.minecraftforge.event.TickEvent;
            import net.minecraftforge.event.entity.player.PlayerEvent;
            import net.minecraftforge.event.server.ServerStartedEvent;
            import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
            import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
            import net.minecraftforge.fml.common.Mod;
            import net.minecraftforge.registries.RegisterEvent;

            @Mod("%s")
            public final class ForgeEntrypoint {
                private long serverTicks;
                private long clientTicks;

                public ForgeEntrypoint() {
                    NexusGeneratedBootstrap.init();
                    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegister);
                    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
                    MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
                    MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
                    MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
                    MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
                    MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
                }

                private void onClientSetup(FMLClientSetupEvent event) {
                    NexusGeneratedBootstrap.client(event);
                }

                private void onRegister(RegisterEvent event) {
                    NexusGeneratedBootstrap.registries(event);
                }

                private void onRegisterCommands(RegisterCommandsEvent event) {
                    NexusGeneratedBootstrap.commands(event.getDispatcher());
                }

                private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
                    NexusGeneratedBootstrap.playerJoined(event.getEntity(), event.getEntity().getName().getString());
                }

                private void onServerTick(TickEvent.ServerTickEvent event) {
                    if (event.phase == TickEvent.Phase.END) {
                        NexusGeneratedBootstrap.serverTick(++serverTicks);
                    }
                }

                private void onClientTick(TickEvent.ClientTickEvent event) {
                    if (event.phase == TickEvent.Phase.END) {
                        NexusGeneratedBootstrap.clientTick(++clientTicks);
                    }
                }

                private void onServerStarted(ServerStartedEvent event) {
                    NexusGeneratedBootstrap.server(event.getServer());
                }
            }
            """.formatted(generatedPackage(), getModId().get());
    }

    private String neoForgeEntrypoint() {
        return """
            package %s;

            import net.neoforged.bus.api.IEventBus;
            import net.neoforged.fml.common.Mod;
            import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
            import net.neoforged.neoforge.client.event.ClientTickEvent;
            import net.neoforged.neoforge.common.NeoForge;
            import net.neoforged.neoforge.event.RegisterCommandsEvent;
            import net.neoforged.neoforge.event.entity.player.PlayerEvent;
            import net.neoforged.neoforge.event.server.ServerStartedEvent;
            import net.neoforged.neoforge.event.tick.ServerTickEvent;
            import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
            import net.neoforged.neoforge.registries.RegisterEvent;

            @Mod("%s")
            public final class NeoForgeEntrypoint {
                private long serverTicks;
                private long clientTicks;

                public NeoForgeEntrypoint(IEventBus modBus) {
                    NexusGeneratedBootstrap.init();
                    modBus.addListener(this::onRegister);
                    modBus.addListener(this::onClientSetup);
                    modBus.addListener(this::onRegisterPayloadHandlers);
                    NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
                    NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
                    NeoForge.EVENT_BUS.addListener(this::onServerTick);
                    NeoForge.EVENT_BUS.addListener(this::onClientTick);
                    NeoForge.EVENT_BUS.addListener(this::onServerStarted);
                }

                private void onClientSetup(FMLClientSetupEvent event) {
                    NexusGeneratedBootstrap.client(event);
                }

                private void onRegisterCommands(RegisterCommandsEvent event) {
                    NexusGeneratedBootstrap.commands(event.getDispatcher());
                }

                private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
                    NexusGeneratedBootstrap.networkPayloads(event);
                }

                private void onRegister(RegisterEvent event) {
                    NexusGeneratedBootstrap.registries(event);
                }

                private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
                    NexusGeneratedBootstrap.playerJoined(event.getEntity(), event.getEntity().getName().getString());
                }

                private void onServerTick(ServerTickEvent.Post event) {
                    NexusGeneratedBootstrap.serverTick(++serverTicks);
                }

                private void onClientTick(ClientTickEvent.Post event) {
                    NexusGeneratedBootstrap.clientTick(++clientTicks);
                }

                private void onServerStarted(ServerStartedEvent event) {
                    NexusGeneratedBootstrap.server(event.getServer());
                }
            }
            """.formatted(generatedPackage(), getModId().get());
    }

    private String escapeJava(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String gradleProperties() {
        String javaHome = detectedGradleJavaHome(javaLanguageVersion());
        return """
            org.gradle.jvmargs=-Xmx3G -Dfile.encoding=UTF-8
            org.gradle.parallel=true
            %s
            """.formatted(javaHome == null ? "" : "org.gradle.java.home=" + javaHome);
    }

    private String detectedGradleJavaHome(int requiredJavaVersion) {
        if (requiredJavaVersion <= Runtime.version().feature()) {
            return null;
        }
        return findJavaHome(requiredJavaVersion);
    }

    private String findJavaHome(int requiredJavaVersion) {
        List<String> candidates = new ArrayList<>();
        addEnvironmentCandidate(candidates, "NEXUSCORE_JAVA" + requiredJavaVersion + "_HOME");
        addEnvironmentCandidate(candidates, "JAVA" + requiredJavaVersion + "_HOME");
        addEnvironmentCandidate(candidates, "JAVA_HOME");
        addDirectoryChildren(candidates, new File(System.getProperty("user.home"), ".jdks"));
        addDirectoryChildren(candidates, new File(System.getProperty("user.home"), ".gradle/jdks"));
        addDirectoryChildren(candidates, new File("C:/Program Files/Eclipse Adoptium"));
        addDirectoryChildren(candidates, new File("C:/Program Files/Java"));
        for (String candidate : candidates) {
            if (isJavaHome(candidate, requiredJavaVersion)) {
                return new File(candidate).getAbsolutePath().replace("\\", "/");
            }
        }
        return null;
    }

    private void addEnvironmentCandidate(List<String> candidates, String variableName) {
        String value = System.getenv(variableName);
        if (value != null && !value.isBlank()) {
            candidates.add(value);
        }
    }

    private void addDirectoryChildren(List<String> candidates, File directory) {
        File[] children = directory.listFiles(File::isDirectory);
        if (children == null) {
            return;
        }
        for (File child : children) {
            candidates.add(child.getAbsolutePath());
        }
    }

    private boolean isJavaHome(String candidate, int requiredJavaVersion) {
        File javaExecutable = new File(candidate, "bin/java.exe");
        if (!javaExecutable.isFile()) {
            javaExecutable = new File(candidate, "bin/java");
        }
        if (!javaExecutable.isFile()) {
            return false;
        }
        try {
            Process process = new ProcessBuilder(javaExecutable.getAbsolutePath(), "-version")
                .redirectErrorStream(true)
                .start();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return false;
            }
            String versionOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return process.exitValue() == 0 && parseJavaFeature(versionOutput) >= requiredJavaVersion;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    private int parseJavaFeature(String versionOutput) {
        int firstQuote = versionOutput.indexOf('"');
        int secondQuote = firstQuote < 0 ? -1 : versionOutput.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) {
            return 0;
        }
        String version = versionOutput.substring(firstQuote + 1, secondQuote);
        if (version.startsWith("1.")) {
            int end = version.indexOf('.', 2);
            return Integer.parseInt(end < 0 ? version.substring(2) : version.substring(2, end));
        }
        int end = version.indexOf('.');
        return Integer.parseInt(end < 0 ? version : version.substring(0, end));
    }

    private String readme() {
        return """
            # %s

            Generated NexusCore target project for `%s`.

            Build:

            ```bash
            ./gradlew build
            ```

            Launch smoke tests:

            ```bash
            ./gradlew runClient
            ./gradlew runServer
            ```
            """.formatted(getTargetId().get(), getLoader().get() + " " + getMinecraftVersion().get());
    }

    private void writeGradleWrapper(Path output) throws IOException {
        copyWrapperFile("gradlew", output.resolve("gradlew"), true);
        copyWrapperFile("gradlew.bat", output.resolve("gradlew.bat"), false);
        Path wrapperDirectory = output.resolve("gradle/wrapper");
        Files.createDirectories(wrapperDirectory);
        copyWrapperFile("gradle/wrapper/gradle-wrapper.jar", wrapperDirectory.resolve("gradle-wrapper.jar"), false);
        Files.writeString(wrapperDirectory.resolve("gradle-wrapper.properties"), gradleWrapperProperties());
    }

    private void copyWrapperFile(String relativePath, Path output, boolean executable) throws IOException {
        File source = findAncestorFile(relativePath);
        if (source == null) {
            return;
        }
        Files.createDirectories(output.getParent());
        Files.copy(source.toPath(), output, StandardCopyOption.REPLACE_EXISTING);
        if (executable) {
            output.toFile().setExecutable(true, false);
        }
    }

    private File findAncestorFile(String relativePath) {
        File directory = getProject().getProjectDir();
        while (directory != null) {
            File file = new File(directory, relativePath);
            if (file.isFile()) {
                return file;
            }
            directory = directory.getParentFile();
        }
        return null;
    }

    private String gradleWrapperProperties() {
        return """
            distributionBase=GRADLE_USER_HOME
            distributionPath=wrapper/dists
            distributionUrl=https\\://services.gradle.org/distributions/gradle-%s-bin.zip
            networkTimeout=10000
            validateDistributionUrl=true
            zipStoreBase=GRADLE_USER_HOME
            zipStorePath=wrapper/dists
            """.formatted(gradleDistributionVersion());
    }

    private String gradleDistributionVersion() {
        if ("26.1.2".equals(getMinecraftVersion().get())) {
            return "9.5.1";
        }
        return "8.14.3";
    }
}
