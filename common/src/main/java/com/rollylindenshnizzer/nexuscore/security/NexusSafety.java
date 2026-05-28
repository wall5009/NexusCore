package com.rollylindenshnizzer.nexuscore.security;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

@NexusStable(since = "1.3")
public final class NexusSafety {
    public static AutomationSecurity automation() {
        return new AutomationSecurity(1_000, 256, true);
    }

    public static TeleportSafety teleport() {
        return new TeleportSafety(-64, 320, 16, true);
    }

    public static DataDefinitionSafety dataDefinitions() {
        return new DataDefinitionSafety(false, 256_000, true);
    }

    public static AiSafety ai() {
        return new AiSafety(128, 32, true);
    }

    public static MultiblockSafety multiblocks() {
        return new MultiblockSafety(4_096, 512, true, true);
    }

    public static RitualSafety rituals() {
        return new RitualSafety(256, 16, 16_000, true, false);
    }

    public static VisualToolSafety visualTools() {
        return new VisualToolSafety(false, true, true, List.of("src/generated/resources", "build/generated/nexus"));
    }

    @NexusStable(since = "1.3")
    public record AutomationSecurity(int maxTransfersPerTick, int maxNetworkNodes, boolean blockCrossClaimRoutes) {
        public List<String> validate(int transfersPerTick, int networkNodes) {
            List<String> errors = new ArrayList<>();
            if (transfersPerTick > maxTransfersPerTick) {
                errors.add("transfers per tick exceeds " + maxTransfersPerTick);
            }
            if (networkNodes > maxNetworkNodes) {
                errors.add("network nodes exceeds " + maxNetworkNodes);
            }
            return errors;
        }
    }

    @NexusStable(since = "1.3")
    public record TeleportSafety(int minY, int maxY, int maxPlatformRadius, boolean requireLoadedChunk) {
        public boolean safe(BlockPos pos, int platformRadius) {
            return pos.getY() >= minY && pos.getY() <= maxY && platformRadius <= maxPlatformRadius;
        }
    }

    @NexusStable(since = "1.3")
    public record DataDefinitionSafety(boolean allowUnsafeCommands, int maxJsonLength, boolean requireSchema) {
        public List<String> validate(String json, boolean hasSchema, boolean hasUnsafeCommand) {
            List<String> errors = new ArrayList<>();
            if (json.length() > maxJsonLength) {
                errors.add("definition exceeds max length " + maxJsonLength);
            }
            if (requireSchema && !hasSchema) {
                errors.add("definition requires a schema");
            }
            if (!allowUnsafeCommands && hasUnsafeCommand) {
                errors.add("unsafe command is disabled");
            }
            return errors;
        }
    }

    @NexusStable(since = "1.3")
    public record AiSafety(int maxPathLength, int maxGoalsPerEntity, boolean detectStuckEntities) {
        public List<String> validate(int pathLength, int goals) {
            List<String> errors = new ArrayList<>();
            if (pathLength > maxPathLength) {
                errors.add("path length exceeds " + maxPathLength);
            }
            if (goals > maxGoalsPerEntity) {
                errors.add("goal count exceeds " + maxGoalsPerEntity);
            }
            return errors;
        }
    }

    @NexusStable(since = "1.3")
    public record MultiblockSafety(int maxVolume,
                                   int maxRevalidationCost,
                                   boolean serverAuthoritative,
                                   boolean respectClaims) {
        public List<String> validate(int volume, int revalidationCost, boolean clientAuthoritative) {
            List<String> errors = new ArrayList<>();
            if (volume > maxVolume) {
                errors.add("multiblock volume exceeds " + maxVolume);
            }
            if (revalidationCost > maxRevalidationCost) {
                errors.add("multiblock revalidation cost exceeds " + maxRevalidationCost);
            }
            if (serverAuthoritative && clientAuthoritative) {
                errors.add("client-only assembly state cannot be authoritative");
            }
            return errors;
        }
    }

    @NexusStable(since = "1.3")
    public record RitualSafety(int maxAreaBlocks,
                               int maxEntitySpawns,
                               int maxTeleportDistance,
                               boolean respectClaims,
                               boolean dangerousEffectsOptIn) {
        public List<String> validate(int areaBlocks, int entitySpawns, int teleportDistance, boolean dangerousEffect) {
            List<String> errors = new ArrayList<>();
            if (areaBlocks > maxAreaBlocks) {
                errors.add("ritual area transform exceeds " + maxAreaBlocks + " blocks");
            }
            if (entitySpawns > maxEntitySpawns) {
                errors.add("ritual entity spawn count exceeds " + maxEntitySpawns);
            }
            if (teleportDistance > maxTeleportDistance) {
                errors.add("ritual teleport distance exceeds " + maxTeleportDistance);
            }
            if (dangerousEffect && !dangerousEffectsOptIn) {
                errors.add("dangerous ritual effects require explicit opt-in");
            }
            return errors;
        }
    }

    @NexusStable(since = "1.3")
    public record VisualToolSafety(boolean enabledInProduction,
                                   boolean requirePermission,
                                   boolean confirmOverwrites,
                                   List<String> approvedExportFolders) {
        public VisualToolSafety {
            approvedExportFolders = List.copyOf(approvedExportFolders);
        }

        public List<String> validate(boolean production, boolean hasPermission, String folder, boolean overwriteConfirmed) {
            List<String> errors = new ArrayList<>();
            if (production && !enabledInProduction) {
                errors.add("visual authoring tools are disabled in production");
            }
            if (requirePermission && !hasPermission) {
                errors.add("visual authoring permission is required");
            }
            if (approvedExportFolders.stream().noneMatch(folder::startsWith)) {
                errors.add("export folder is not approved");
            }
            if (confirmOverwrites && !overwriteConfirmed) {
                errors.add("overwrite confirmation is required");
            }
            return errors;
        }
    }

    private NexusSafety() {
    }
}
