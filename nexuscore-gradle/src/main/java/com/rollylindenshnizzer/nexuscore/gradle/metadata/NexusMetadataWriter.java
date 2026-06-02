package com.rollylindenshnizzer.nexuscore.gradle.metadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public final class NexusMetadataWriter {
    private NexusMetadataWriter() {
    }

    public static void write(NexusMetadataModel model, Path outputDirectory) throws IOException {
        Files.createDirectories(outputDirectory);
        Files.writeString(outputDirectory.resolve("pack.mcmeta"), pack(model));
        Files.writeString(outputDirectory.resolve("mixins." + model.modId() + ".json"), mixins(model));

        switch (model.loader()) {
            case "fabric" -> Files.writeString(outputDirectory.resolve("fabric.mod.json"), fabric(model));
            case "quilt" -> Files.writeString(outputDirectory.resolve("quilt.mod.json"), quilt(model));
            case "forge" -> {
                Path metaInf = outputDirectory.resolve("META-INF");
                Files.createDirectories(metaInf);
                Files.writeString(metaInf.resolve("mods.toml"), forge(model));
            }
            case "neoforge" -> {
                Path metaInf = outputDirectory.resolve("META-INF");
                Files.createDirectories(metaInf);
                Files.writeString(metaInf.resolve("neoforge.mods.toml"), neoForge(model));
            }
            default -> throw new IllegalArgumentException("Unsupported loader for metadata generation: " + model.loader());
        }
    }

    private static String pack(NexusMetadataModel model) {
        return """
            {
              "pack": {
                "pack_format": 15,
                "description": "%s generated resources"
              }
            }
            """.formatted(escape(model.displayName()));
    }

    private static String mixins(NexusMetadataModel model) {
        return """
            {
              "required": false,
              "package": "%s.mixin",
              "compatibilityLevel": "JAVA_17",
              "mixins": [],
              "client": [],
              "injectors": {
                "defaultRequire": 1
              }
            }
            """.formatted(escape(model.modId()));
    }

    private static String fabric(NexusMetadataModel model) {
        return """
            {
              "schemaVersion": 1,
              "id": "%s",
              "version": "%s",
              "name": "%s",
              "description": "%s",
              "authors": [%s],
              "license": "%s",
              "environment": "*",
              "entrypoints": {
                "main": [
                  "%s"
                ],
                "client": [
                  "%s"
                ],
                "server": [
                  "%s"
                ]
              },
              "mixins": [
                "mixins.%s.json"
              ],
              "depends": {
                "minecraft": "%s"
              }
            }
            """.formatted(
                model.modId(),
                model.version(),
                escape(model.displayName()),
                escape(model.description()),
                authors(model),
                escape(model.license()),
                generatedPackage(model) + ".FabricEntrypoint",
                generatedPackage(model) + ".FabricClientEntrypoint",
                generatedPackage(model) + ".FabricServerEntrypoint",
                model.modId(),
                model.minecraftVersion()
            );
    }

    private static String quilt(NexusMetadataModel model) {
        return """
            {
              "schema_version": 1,
              "quilt_loader": {
                "group": "generated",
                "id": "%s",
                "version": "%s",
                "metadata": {
                  "name": "%s",
                  "description": "%s",
                  "contributors": {%s},
                  "license": "%s"
                },
                "intermediate_mappings": "net.fabricmc:intermediary",
                "entrypoints": {
                  "main": [
                    "%s"
                  ],
                  "client": [
                    "%s"
                  ],
                  "server": [
                    "%s"
                  ]
                }
              },
              "minecraft": {
                "version": "%s"
              },
              "mixin": [
                "mixins.%s.json"
              ]
            }
            """.formatted(
                model.modId(),
                model.version(),
                escape(model.displayName()),
                escape(model.description()),
                quiltAuthors(model),
                escape(model.license()),
                generatedPackage(model) + ".QuiltEntrypoint",
                generatedPackage(model) + ".QuiltClientEntrypoint",
                generatedPackage(model) + ".QuiltServerEntrypoint",
                model.minecraftVersion(),
                model.modId()
            );
    }

    private static String forge(NexusMetadataModel model) {
        return """
            modLoader="javafml"
            loaderVersion="[47,)"
            license="%s"

            [[mods]]
            modId="%s"
            version="%s"
            displayName="%s"
            authors="%s"
            description='''%s'''

            [[dependencies.%s]]
            modId="minecraft"
            mandatory=true
            versionRange="[%s]"
            ordering="NONE"
            side="BOTH"
            """.formatted(escape(model.license()), model.modId(), model.version(), escape(model.displayName()), String.join(", ", model.authors()), escape(model.description()), model.modId(), model.minecraftVersion());
    }

    private static String neoForge(NexusMetadataModel model) {
        return """
            modLoader="javafml"
            loaderVersion="[4,)"
            license="%s"

            [[mods]]
            modId="%s"
            version="%s"
            displayName="%s"
            authors="%s"
            description='''%s'''

            [[dependencies.%s]]
            modId="minecraft"
            type="required"
            versionRange="[%s]"
            ordering="NONE"
            side="BOTH"
            """.formatted(escape(model.license()), model.modId(), model.version(), escape(model.displayName()), String.join(", ", model.authors()), escape(model.description()), model.modId(), model.minecraftVersion());
    }

    private static String authors(NexusMetadataModel model) {
        return model.authors().stream().map(author -> "\"" + escape(author) + "\"").collect(Collectors.joining(", "));
    }

    private static String quiltAuthors(NexusMetadataModel model) {
        return model.authors().stream().map(author -> "\"" + escape(author) + "\": \"Owner\"").collect(Collectors.joining(", "));
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String generatedPackage(NexusMetadataModel model) {
        return "com.rollylindenshnizzer.nexuscore.generated." + model.targetId();
    }
}
