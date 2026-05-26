package com.rollylindenshnizzer.nexuscore.core;

import dev.architectury.platform.Platform;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public final class NexusDiagnostics {
    public static Report startup(String modId) {
        Report report = new Report("startup", modId);
        report.info("minecraft", Platform.getMinecraftVersion());
        report.info("loader", loaderName());
        report.info("environment", NexusEnvironment.physicalSide().name());
        report.info("development", Boolean.toString(NexusEnvironment.isDevelopment()));
        report.info("datagen", Boolean.toString(NexusEnvironment.isDataGeneration()));
        report.info("test", Boolean.toString(NexusEnvironment.isTestEnvironment()));
        return report;
    }

    public static Report report(String type, String modId) {
        return new Report(type, modId);
    }

    private static String loaderName() {
        if (Platform.isFabric()) {
            return "fabric";
        }
        if (Platform.isNeoForge()) {
            return "neoforge";
        }
        if (Platform.isMinecraftForge()) {
            return "forge";
        }
        return "unknown";
    }

    private NexusDiagnostics() {
    }

    public static final class Report {
        private final String type;
        private final String modId;
        private final List<String> lines = new ArrayList<>();
        private int warnings;
        private int errors;

        private Report(String type, String modId) {
            this.type = type;
            this.modId = modId;
        }

        public Report info(String key, String value) {
            lines.add(key + "=" + value);
            return this;
        }

        public Report warning(String message) {
            warnings++;
            lines.add("warning: " + message);
            return this;
        }

        public Report error(String message) {
            errors++;
            lines.add("error: " + message);
            return this;
        }

        public boolean hasErrors() {
            return errors > 0;
        }

        public String summary() {
            return "NexusCore " + type + " report for " + modId + ": " + lines + " warnings=" + warnings + " errors=" + errors;
        }

        public void log(Logger logger) {
            if (errors > 0) {
                logger.error(summary());
            } else if (warnings > 0) {
                logger.warn(summary());
            } else {
                NexusLoggers.debugOnly(logger, summary());
            }
        }
    }
}
