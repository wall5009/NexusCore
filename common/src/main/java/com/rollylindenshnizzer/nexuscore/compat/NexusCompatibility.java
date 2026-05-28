package com.rollylindenshnizzer.nexuscore.compat;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@NexusStable(since = "1.3")
public final class NexusCompatibility {
    private static final Map<String, IntegrationNode> INTEGRATIONS = new LinkedHashMap<>();

    public static IntegrationNode.Builder integration(String modId) {
        return new IntegrationNode.Builder(modId);
    }

    public static IntegrationNode register(IntegrationNode node) {
        INTEGRATIONS.put(node.modId(), node);
        return node;
    }

    public static Collection<IntegrationNode> integrations() {
        return List.copyOf(INTEGRATIONS.values());
    }

    public static Optional<IntegrationNode> find(String modId) {
        return Optional.ofNullable(INTEGRATIONS.get(modId));
    }

    public static CompatibilityMatrix matrix() {
        return new CompatibilityMatrix(List.copyOf(INTEGRATIONS.values()));
    }

    public static String debugSummary() {
        CompatibilityMatrix matrix = matrix();
        return "integrations=" + matrix.integrations().size() + ", active=" + matrix.activeCount()
                + ", dashboardWarnings=" + matrix.dashboard().warnings().size();
    }

    @NexusStable(since = "1.3")
    public record IntegrationNode(String modId,
                                  boolean loaded,
                                  List<String> recipeViewers,
                                  List<String> tooltipProviders,
                                  List<String> equipmentApis,
                                  List<String> permissionApis,
                                  List<String> mappingDocs,
                                  List<String> warnings) {
        public IntegrationNode {
            recipeViewers = List.copyOf(recipeViewers);
            tooltipProviders = List.copyOf(tooltipProviders);
            equipmentApis = List.copyOf(equipmentApis);
            permissionApis = List.copyOf(permissionApis);
            mappingDocs = List.copyOf(mappingDocs);
            warnings = List.copyOf(warnings);
        }

        public IntegrationReport report() {
            List<String> features = new ArrayList<>();
            features.addAll(recipeViewers);
            features.addAll(tooltipProviders);
            features.addAll(equipmentApis);
            features.addAll(permissionApis);
            features.addAll(mappingDocs);
            return new IntegrationReport(modId, loaded, features, warnings);
        }

        public static final class Builder {
            private final String modId;
            private boolean loaded;
            private final List<String> recipeViewers = new ArrayList<>();
            private final List<String> tooltipProviders = new ArrayList<>();
            private final List<String> equipmentApis = new ArrayList<>();
            private final List<String> permissionApis = new ArrayList<>();
            private final List<String> mappingDocs = new ArrayList<>();
            private final List<String> warnings = new ArrayList<>();

            private Builder(String modId) {
                this.modId = modId;
            }

            public Builder loaded(boolean loaded) {
                this.loaded = loaded;
                return this;
            }

            public Builder recipeViewer(String viewer) {
                this.recipeViewers.add(viewer);
                return this;
            }

            public Builder tooltipProvider(String provider) {
                this.tooltipProviders.add(provider);
                return this;
            }

            public Builder equipmentApi(String api) {
                this.equipmentApis.add(api);
                return this;
            }

            public Builder permissionApi(String api) {
                this.permissionApis.add(api);
                return this;
            }

            public Builder mappingDocs(String docs) {
                this.mappingDocs.add(docs);
                return this;
            }

            public Builder warning(String warning) {
                this.warnings.add(warning);
                return this;
            }

            public IntegrationNode build() {
                return new IntegrationNode(modId, loaded, recipeViewers, tooltipProviders,
                        equipmentApis, permissionApis, mappingDocs, warnings);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record IntegrationReport(String modId, boolean loaded, List<String> features, List<String> warnings) {
        public IntegrationReport {
            features = List.copyOf(features);
            warnings = List.copyOf(warnings);
        }
    }

    @NexusStable(since = "1.3")
    public record CompatibilityMatrix(List<IntegrationNode> integrations) {
        public CompatibilityMatrix {
            integrations = List.copyOf(integrations);
        }

        public long activeCount() {
            return integrations.stream().filter(IntegrationNode::loaded).count();
        }

        public List<IntegrationReport> reports() {
            return integrations.stream().map(IntegrationNode::report).toList();
        }

        public CompatibilityDashboard dashboard() {
            List<String> loaded = integrations.stream().filter(IntegrationNode::loaded).map(IntegrationNode::modId).toList();
            List<String> missing = integrations.stream().filter(node -> !node.loaded()).map(IntegrationNode::modId).toList();
            List<String> warnings = integrations.stream().flatMap(node -> node.warnings().stream()).toList();
            return new CompatibilityDashboard(loaded, missing, List.of(), List.of(), warnings);
        }
    }

    @NexusStable(since = "1.3")
    public record TooltipBridge(String providerId, List<String> lines, boolean supportsRichText, boolean supportsClientOnlyData) {
        public TooltipBridge {
            lines = List.copyOf(lines);
        }
    }

    @NexusStable(since = "1.3")
    public record EquipmentBridge(String apiId, List<String> slots, boolean supportsCosmeticSlots, boolean supportsAttributeSync) {
        public EquipmentBridge {
            slots = List.copyOf(slots);
        }
    }

    @NexusStable(since = "1.3")
    public record PermissionBridge(String apiId, String defaultPolicy, List<String> claims, List<String> requiredNodes) {
        public PermissionBridge {
            claims = List.copyOf(claims);
            requiredNodes = List.copyOf(requiredNodes);
        }
    }

    @NexusStable(since = "1.3")
    public record ProtectionBridge(String apiId,
                                   boolean loaded,
                                   boolean failSafely,
                                   List<ProtectionAction> supportedActions,
                                   List<String> warnings) {
        public ProtectionBridge {
            supportedActions = List.copyOf(supportedActions);
            warnings = List.copyOf(warnings);
        }

        public ProtectionResult check(ProtectionAction action, String actor, BlockPos pos) {
            if (!loaded && failSafely) {
                return new ProtectionResult(action, actor, pos, false, "bridge_missing");
            }
            if (!supportedActions.contains(action)) {
                return new ProtectionResult(action, actor, pos, false, "action_not_supported");
            }
            return new ProtectionResult(action, actor, pos, true, "allowed");
        }
    }

    @NexusStable(since = "1.3")
    public enum ProtectionAction {
        BLOCK_BREAK,
        BLOCK_PLACE,
        BLOCK_INTERACT,
        ENTITY_INTERACT,
        EXPLOSION,
        RITUAL_EFFECT,
        AUTOMATION_EXTRACT,
        AUTOMATION_INSERT,
        STRUCTURE_PLACE;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusStable(since = "1.3")
    public record ProtectionResult(ProtectionAction action, String actor, BlockPos pos, boolean allowed, String reason) {
    }

    @NexusStable(since = "1.3")
    public record MapMarkerBridge(String apiId,
                                  boolean enabled,
                                  List<MapMarker> markers,
                                  boolean serverCanDisable) {
        public MapMarkerBridge {
            markers = List.copyOf(markers);
        }
    }

    @NexusStable(since = "1.3")
    public record MapMarker(ResourceLocation id, MarkerKind kind, BlockPos pos, String dimension, String label) {
    }

    @NexusStable(since = "1.3")
    public enum MarkerKind {
        STRUCTURE,
        RITUAL,
        DIMENSION_PORTAL,
        BOSS_ARENA
    }

    @NexusStable(since = "1.3")
    public record GuidebookBridge(String apiId,
                                  boolean loaded,
                                  List<GuidebookPage> pages,
                                  boolean fallbackScreenEnabled,
                                  List<String> diagnostics) {
        public GuidebookBridge {
            pages = List.copyOf(pages);
            diagnostics = List.copyOf(diagnostics);
        }
    }

    @NexusStable(since = "1.3")
    public record GuidebookPage(ResourceLocation id,
                                String title,
                                GuidebookSubject subject,
                                List<String> lines,
                                boolean visibleWhenUnlocked) {
        public GuidebookPage {
            lines = List.copyOf(lines);
        }
    }

    @NexusStable(since = "1.3")
    public enum GuidebookSubject {
        MACHINE,
        MULTIBLOCK,
        RITUAL,
        DIMENSION,
        STRUCTURE,
        ENTITY,
        AUTOMATION_NETWORK,
        PROGRESSION
    }

    @NexusStable(since = "1.3")
    public record RecipeTransferBridge(String viewerId,
                                       boolean ghostIngredients,
                                       boolean missingItemHints,
                                       boolean fluidRequirementDisplay,
                                       boolean energyRequirementDisplay,
                                       List<String> categories,
                                       List<String> failureReasons) {
        public RecipeTransferBridge {
            categories = List.copyOf(categories);
            failureReasons = List.copyOf(failureReasons);
        }

        public boolean supportsCategory(String category) {
            return categories.contains(category);
        }
    }

    @NexusStable(since = "1.3")
    public record AccessoryBridge(String apiId,
                                  boolean loaded,
                                  List<String> vanillaSlots,
                                  List<String> accessorySlots,
                                  boolean cosmeticSlots,
                                  boolean attributesSynced) {
        public AccessoryBridge {
            vanillaSlots = List.copyOf(vanillaSlots);
            accessorySlots = List.copyOf(accessorySlots);
        }
    }

    @NexusStable(since = "1.3")
    public record PermissionNode(String node, PermissionTarget target, boolean defaultAllowed, String description) {
        public static PermissionNode adminTools() {
            return new PermissionNode("nexuscore.admin_tools", PermissionTarget.ADMIN_TOOL, false, "Use NexusCore developer and authoring tools.");
        }

        public static PermissionNode ritualUse() {
            return new PermissionNode("nexuscore.ritual.use", PermissionTarget.RITUAL, true, "Start safe rituals.");
        }

        public static PermissionNode multiblockEdit() {
            return new PermissionNode("nexuscore.multiblock.edit", PermissionTarget.MULTIBLOCK, false, "Export or edit multiblock definitions.");
        }
    }

    @NexusStable(since = "1.3")
    public enum PermissionTarget {
        ADMIN_TOOL,
        RITUAL,
        MULTIBLOCK,
        AUTOMATION,
        GUIDEBOOK
    }

    @NexusStable(since = "1.3")
    public record CompatibilityDashboard(List<String> loadedOptionalBridges,
                                         List<String> missingOptionalMods,
                                         List<String> incompatibleVersions,
                                         List<String> disabledIntegrations,
                                         List<String> warnings) {
        public CompatibilityDashboard {
            loadedOptionalBridges = List.copyOf(loadedOptionalBridges);
            missingOptionalMods = List.copyOf(missingOptionalMods);
            incompatibleVersions = List.copyOf(incompatibleVersions);
            disabledIntegrations = List.copyOf(disabledIntegrations);
            warnings = List.copyOf(warnings);
        }

        public String summary() {
            return "loaded=" + loadedOptionalBridges.size()
                    + ", missing=" + missingOptionalMods.size()
                    + ", incompatible=" + incompatibleVersions.size()
                    + ", disabled=" + disabledIntegrations.size()
                    + ", warnings=" + warnings.size();
        }
    }

    private NexusCompatibility() {
    }
}
