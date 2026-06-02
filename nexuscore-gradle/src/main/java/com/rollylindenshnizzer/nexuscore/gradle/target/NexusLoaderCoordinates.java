package com.rollylindenshnizzer.nexuscore.gradle.target;

import org.gradle.api.Project;

public final class NexusLoaderCoordinates {
    private NexusLoaderCoordinates() {
    }

    public static String value(Project project, String name, String fallback) {
        Object property = project.findProperty(name);
        return property == null ? fallback : property.toString();
    }

    public static String loomVersion(Project project) {
        return value(project, "nexuscore.loomVersion", "1.10.5");
    }

    public static String loomVersion(Project project, String minecraftVersion) {
        String fallback = switch (minecraftVersion) {
            case "26.1.2" -> "1.17.0-alpha.14";
            default -> "1.10.5";
        };
        return value(project, "nexuscore.loomVersion." + key(minecraftVersion), value(project, "nexuscore.loomVersion", fallback));
    }

    public static String quiltLoomVersion(Project project) {
        return value(project, "nexuscore.quiltLoomVersion", "1.8.5");
    }

    public static String forgeGradleVersion(Project project) {
        return value(project, "nexuscore.forgeGradleVersion", "[6.0,6.2)");
    }

    public static String modDevGradleVersion(Project project) {
        return value(project, "nexuscore.modDevGradleVersion", "2.0.141");
    }

    public static String fabricLoader(Project project) {
        return fabricLoader(project, "");
    }

    public static String fabricLoader(Project project, String minecraftVersion) {
        String fallback = switch (minecraftVersion) {
            case "26.1.2" -> "0.19.2";
            default -> "0.16.14";
        };
        return value(project, "nexuscore.fabricLoader." + key(minecraftVersion), value(project, "nexuscore.fabricLoaderVersion", fallback));
    }

    public static String quiltLoader(Project project) {
        return value(project, "nexuscore.quiltLoaderVersion", "0.28.0");
    }

    public static String fabricApi(Project project, String minecraftVersion) {
        String fallback = switch (minecraftVersion) {
            case "1.20.1" -> "0.92.2+1.20.1";
            case "1.21.1" -> "0.116.12+1.21.1";
            case "26.1.2" -> "0.150.0+26.1.2";
            default -> "0.92.2+1.20.1";
        };
        return value(project, "nexuscore.fabricApi." + key(minecraftVersion), value(project, "nexuscore.fabricApiVersion", fallback));
    }

    public static String quiltedFabricApi(Project project, String minecraftVersion) {
        String fallback = switch (minecraftVersion) {
            case "1.20.1" -> "7.7.0+0.92.2-1.20.1";
            case "1.21.1" -> "11.0.0-alpha.3+0.102.0-1.21";
            default -> "7.7.0+0.92.2-1.20.1";
        };
        return value(project, "nexuscore.quiltedFabricApi." + key(minecraftVersion), value(project, "nexuscore.quiltedFabricApiVersion", fallback));
    }

    public static String forgeVersion(Project project, String minecraftVersion) {
        return value(project, "nexuscore.forge." + key(minecraftVersion), minecraftVersion + "-47.3.12");
    }

    public static String neoForgeVersion(Project project, String minecraftVersion) {
        String fallback = switch (minecraftVersion) {
            case "1.21.1" -> "21.1.209";
            case "26.1.2" -> "26.1.2.70-beta";
            default -> minecraftVersion;
        };
        return value(project, "nexuscore.neoforge." + key(minecraftVersion), fallback);
    }

    public static String nexusCoreVersion(Project project) {
        return value(project, "nexuscore.version", "2.0.0");
    }

    private static String key(String minecraftVersion) {
        return minecraftVersion.replace('.', '_');
    }
}
