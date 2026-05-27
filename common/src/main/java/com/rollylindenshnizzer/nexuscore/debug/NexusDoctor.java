package com.rollylindenshnizzer.nexuscore.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rollylindenshnizzer.nexuscore.config.ConfigDependencyGraph;
import com.rollylindenshnizzer.nexuscore.config.NexusConfigRegistry;
import com.rollylindenshnizzer.nexuscore.core.NexusException;
import com.rollylindenshnizzer.nexuscore.core.NexusModuleRegistry;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.data.NexusDataValidator;
import com.rollylindenshnizzer.nexuscore.migration.NexusMigrations;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import com.rollylindenshnizzer.nexuscore.registry.NexusContentManifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NexusDoctor {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Builder create(String modId) {
        return new Builder(modId);
    }

    public static DoctorReport run(String modId) {
        return create(modId)
                .checkModules()
                .checkContent()
                .checkDatagen()
                .checkConfigs()
                .checkPackets()
                .checkMigrations()
                .run();
    }

    public static final class Builder {
        private final String modId;
        private boolean modules;
        private boolean content;
        private boolean datagen;
        private boolean configs;
        private boolean packets;
        private boolean migrations;
        private Path reportsPath;

        private Builder(String modId) {
            this.modId = modId;
        }

        public Builder checkModules() {
            modules = true;
            return this;
        }

        public Builder checkContent() {
            content = true;
            return this;
        }

        public Builder checkGeneratedAssets() {
            datagen = true;
            return this;
        }

        public Builder checkTranslations() {
            datagen = true;
            return this;
        }

        public Builder checkDatagen() {
            datagen = true;
            return this;
        }

        public Builder checkConfigSync() {
            configs = true;
            return this;
        }

        public Builder checkConfigs() {
            configs = true;
            return this;
        }

        public Builder checkPackets() {
            packets = true;
            return this;
        }

        public Builder checkMigrations() {
            migrations = true;
            return this;
        }

        public Builder writeReportsTo(String path) {
            reportsPath = Path.of(path);
            return this;
        }

        public DoctorReport run() {
            DoctorReport report = new DoctorReport();
            if (modules) {
                for (String warning : NexusModuleRegistry.incompatibilityWarnings()) {
                    report.warning("NC-DOC-MODULE", "Module version mismatch", warning,
                            "Align NexusCore modules using the v1.1 version catalog or BOM.");
                }
                if (NexusModuleRegistry.incompatibilityWarnings().isEmpty()) {
                    report.info("NC-DOC-MODULE", "Module versions", "All registered Nexus modules are aligned.");
                }
            }
            if (content) {
                int entries = NexusContentManifest.entries(modId).size();
                report.info("NC-DOC-CONTENT", "Content manifest", entries + " manifest entries recorded for " + modId);
            }
            if (datagen) {
                NexusData.DataPlan plan = NexusData.plan(modId);
                var validation = NexusDataValidator.validatePlan(plan);
                validation.issues().forEach(issue -> report.issue(
                        issue.severity() == com.rollylindenshnizzer.nexuscore.data.DataValidationReport.Severity.ERROR
                                ? DoctorSeverity.ERROR : DoctorSeverity.WARNING,
                        "NC-DOC-DATAGEN", "Datagen validation", issue.path() + ": " + issue.message(), issue.suggestion()));
                if (validation.issues().isEmpty()) {
                    report.info("NC-DOC-DATAGEN", "Datagen validation", "No generated data issues found.");
                }
            }
            if (configs) {
                NexusConfigRegistry.configs().stream().filter(config -> config.modId().equals(modId)).forEach(config -> {
                    var graph = ConfigDependencyGraph.analyze(config);
                    if (graph.valid()) {
                        report.info("NC-DOC-CONFIG", "Config graph", config.modId() + " config dependencies are valid.");
                    } else {
                        graph.issues().forEach(issue -> report.error("NC-DOC-CONFIG", "Config graph issue", issue,
                                "Fix dependency/conflict metadata."));
                    }
                });
            }
            if (packets) {
                report.info("NC-DOC-NETWORK", "Packet registrations",
                        NexusNetworking.diagnostics().size() + " Nexus network channels registered.");
            }
            if (migrations) {
                NexusMigrations.diagnostics().forEach((id, diagnostics) -> diagnostics.issues().forEach(issue ->
                        report.issue(DoctorSeverity.valueOf(issue.severity().name()), issue.code(),
                                "Migration issue", id + ": " + issue.subject() + " - " + issue.message(),
                                issue.replacementHint())));
            }
            if (reportsPath != null) {
                write(report, reportsPath);
            }
            return report;
        }

        public void registerCommand() {
            com.rollylindenshnizzer.nexuscore.command.NexusCoreCommands.install();
        }

        private static void write(DoctorReport report, Path path) {
            try {
                Files.createDirectories(path);
                Files.writeString(path.resolve("doctor.md"), report.toMarkdown());
                Files.writeString(path.resolve("doctor.json"), GSON.toJson(report.toJson()));
            } catch (IOException exception) {
                throw new NexusException("Failed to write Nexus Doctor report", exception);
            }
        }
    }

    private NexusDoctor() {
    }
}
