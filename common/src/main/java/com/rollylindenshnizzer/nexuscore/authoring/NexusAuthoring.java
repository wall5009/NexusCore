package com.rollylindenshnizzer.nexuscore.authoring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblocks;
import com.rollylindenshnizzer.nexuscore.ritual.NexusRituals;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@NexusIncubating(since = "1.3")
public final class NexusAuthoring {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, EditorDefinition> EDITORS = new LinkedHashMap<>();
    private static final List<ExportLogEntry> EXPORT_LOG = new ArrayList<>();
    private static AuthoringSafety safety = AuthoringSafety.defaults();

    static {
        register(EditorDefinition.form("definition", "Definition", "registry_id", "tag", "block", "item", "entity", "recipe"));
        register(EditorDefinition.visual("multiblock", "Multiblock", "capture", "controller", "roles", "layers", "java_sample", "json_export"));
        register(EditorDefinition.visual("ritual", "Ritual", "center", "radius", "requirements", "effects", "particles", "guide_draft"));
        register(EditorDefinition.visual("automation", "Automation Network", "nodes", "routes", "filters", "bottlenecks", "priority"));
        register(EditorDefinition.visual("structure", "Structure", "template_capture", "anchor", "processors", "jigsaw_connectors", "bounding_box"));
        register(EditorDefinition.form("balance", "Balance Profile", "metrics", "thresholds", "dashboard"));
    }

    public static EditorDefinition register(EditorDefinition editor) {
        EDITORS.put(editor.id(), editor);
        return editor;
    }

    public static Collection<EditorDefinition> editors() {
        return List.copyOf(EDITORS.values());
    }

    public static Optional<EditorDefinition> editor(String id) {
        return Optional.ofNullable(EDITORS.get(id));
    }

    public static AuthoringSafety safety() {
        return safety;
    }

    public static void safety(AuthoringSafety safety) {
        NexusAuthoring.safety = safety == null ? AuthoringSafety.defaults() : safety;
    }

    public static WorkbenchSession openSession(String user, boolean developmentMode, boolean serverEnabled, boolean readOnly) {
        boolean allowed = (developmentMode || safety.allowOnServers()) && (serverEnabled || !safety.blockServerUseByDefault());
        AuthoringMode mode = !allowed ? AuthoringMode.DISABLED : readOnly ? AuthoringMode.READ_ONLY : AuthoringMode.EDIT;
        return new WorkbenchSession(user, mode, developmentMode, serverEnabled, Instant.now(), List.copyOf(EDITORS.values()));
    }

    public static CapturedMultiblock captureMultiblock(ResourceLocation id,
                                                       BlockPos min,
                                                       BlockPos max,
                                                       BlockPos controller,
                                                       Map<Character, String> palette) {
        int width = Math.abs(max.getX() - min.getX()) + 1;
        int height = Math.abs(max.getY() - min.getY()) + 1;
        int depth = Math.abs(max.getZ() - min.getZ()) + 1;
        List<String> layers = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            layers.add("layer " + y + " width=" + width + " depth=" + depth);
        }
        return new CapturedMultiblock(id, min, max, controller, palette, layers,
                NexusMultiblocks.create(id).controller(palette.values().stream().findFirst().orElse("minecraft:air")).build().javaBuilderSample());
    }

    public static RitualDraft ritualDraft(ResourceLocation id, String center, int radius) {
        NexusRituals.RitualDefinition definition = NexusRituals.create(id)
                .center(center)
                .durationSeconds(10)
                .documentationTag("authoring_draft")
                .build();
        return new RitualDraft(id, center, radius, definition.toJson(), definition.guidePageDraft());
    }

    public static VisualGraph automationGraph(ResourceLocation id, List<GraphNode> nodes, List<GraphEdge> edges) {
        List<String> warnings = new ArrayList<>();
        if (nodes.isEmpty()) {
            warnings.add("network has no nodes");
        }
        if (edges.isEmpty()) {
            warnings.add("network has no routes");
        }
        return new VisualGraph(id, "automation", nodes, edges, warnings);
    }

    public static StructureDraft structureDraft(ResourceLocation id, BlockPos min, BlockPos max, String anchor) {
        return new StructureDraft(id, min, max, anchor,
                List.of("minecraft:empty"), List.of("nexuscore:front", "nexuscore:back"),
                "NexusStructures.structure(\"" + id.getNamespace() + "\", \"" + id.getPath() + "\").template(\"" + id + ".nbt\").build();");
    }

    public static ExportResult exportJson(WorkbenchSession session, Path approvedRoot, String relativePath, JsonObject json, boolean overwrite) {
        if (session.mode() == AuthoringMode.DISABLED) {
            return ExportResult.blocked(relativePath, "authoring disabled");
        }
        if (session.mode() == AuthoringMode.READ_ONLY) {
            return ExportResult.blocked(relativePath, "read-only authoring session");
        }
        Path root = approvedRoot.toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).toAbsolutePath().normalize();
        if (!target.startsWith(root)) {
            return ExportResult.blocked(relativePath, "export target escapes approved root");
        }
        if (!overwrite && Files.exists(target)) {
            return ExportResult.blocked(relativePath, "target exists; overwrite was not confirmed");
        }
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, GSON.toJson(json));
            ExportLogEntry entry = new ExportLogEntry(session.user(), target.toString(), Instant.now());
            EXPORT_LOG.add(entry);
            while (EXPORT_LOG.size() > 256) {
                EXPORT_LOG.removeFirst();
            }
            return new ExportResult(true, target.toString(), "", entry);
        } catch (IOException exception) {
            return ExportResult.blocked(relativePath, exception.getMessage());
        }
    }

    public static List<ExportLogEntry> exportLog() {
        return List.copyOf(EXPORT_LOG);
    }

    public static String debugSummary() {
        return "authoringEditors=" + EDITORS.size() + ", exports=" + EXPORT_LOG.size() + ", safety=" + safety.modeSummary();
    }

    @NexusIncubating(since = "1.3")
    public record WorkbenchSession(String user,
                                   AuthoringMode mode,
                                   boolean developmentMode,
                                   boolean serverEnabled,
                                   Instant openedAt,
                                   List<EditorDefinition> editors) {
        public WorkbenchSession {
            editors = List.copyOf(editors);
        }
    }

    @NexusIncubating(since = "1.3")
    public enum AuthoringMode {
        DISABLED,
        READ_ONLY,
        EDIT;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusIncubating(since = "1.3")
    public record EditorDefinition(String id,
                                   String title,
                                   EditorKind kind,
                                   List<String> controls,
                                   boolean validatesWhileEditing,
                                   boolean supportsPreview,
                                   boolean supportsClipboard,
                                   boolean supportsSafeReload) {
        public EditorDefinition {
            controls = List.copyOf(controls);
        }

        public static EditorDefinition form(String id, String title, String... controls) {
            return new EditorDefinition(id, title, EditorKind.FORM, List.of(controls), true, true, true, true);
        }

        public static EditorDefinition visual(String id, String title, String... controls) {
            return new EditorDefinition(id, title, EditorKind.VISUAL, List.of(controls), true, true, true, true);
        }
    }

    @NexusIncubating(since = "1.3")
    public enum EditorKind {
        FORM,
        VISUAL,
        GRAPH,
        TIMELINE
    }

    @NexusIncubating(since = "1.3")
    public record AuthoringSafety(boolean blockServerUseByDefault,
                                  boolean requireDevelopmentMode,
                                  boolean allowOnServers,
                                  List<String> approvedOutputFolders,
                                  boolean confirmOverwrites,
                                  boolean logExports) {
        public AuthoringSafety {
            approvedOutputFolders = List.copyOf(approvedOutputFolders);
        }

        public static AuthoringSafety defaults() {
            return new AuthoringSafety(true, true, false,
                    List.of("src/generated/resources", "build/generated/nexus", "docs/generated"),
                    true, true);
        }

        public String modeSummary() {
            return "devRequired=" + requireDevelopmentMode + ", serverBlocked=" + blockServerUseByDefault;
        }
    }

    @NexusIncubating(since = "1.3")
    public record CapturedMultiblock(ResourceLocation id,
                                     BlockPos min,
                                     BlockPos max,
                                     BlockPos controller,
                                     Map<Character, String> palette,
                                     List<String> layerPreview,
                                     String javaSample) {
        public CapturedMultiblock {
            palette = Map.copyOf(palette);
            layerPreview = List.copyOf(layerPreview);
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualDraft(ResourceLocation id,
                              String center,
                              int radius,
                              JsonObject json,
                              NexusRituals.GuidePageDraft guidePageDraft) {
        public RitualDraft {
            json = json.deepCopy();
        }
    }

    @NexusIncubating(since = "1.3")
    public record VisualGraph(ResourceLocation id, String kind, List<GraphNode> nodes, List<GraphEdge> edges, List<String> warnings) {
        public VisualGraph {
            nodes = List.copyOf(nodes);
            edges = List.copyOf(edges);
            warnings = List.copyOf(warnings);
        }
    }

    @NexusIncubating(since = "1.3")
    public record GraphNode(String id, BlockPos pos, String type, Map<String, String> properties) {
        public GraphNode {
            properties = Map.copyOf(properties);
        }
    }

    @NexusIncubating(since = "1.3")
    public record GraphEdge(String from, String to, String kind, int priority, Map<String, String> properties) {
        public GraphEdge {
            properties = Map.copyOf(properties);
        }
    }

    @NexusIncubating(since = "1.3")
    public record StructureDraft(ResourceLocation id,
                                 BlockPos min,
                                 BlockPos max,
                                 String anchor,
                                 List<String> processors,
                                 List<String> jigsawConnectors,
                                 String datagenSample) {
        public StructureDraft {
            processors = List.copyOf(processors);
            jigsawConnectors = List.copyOf(jigsawConnectors);
        }
    }

    @NexusIncubating(since = "1.3")
    public record ExportResult(boolean success, String target, String reason, ExportLogEntry logEntry) {
        public static ExportResult blocked(String target, String reason) {
            return new ExportResult(false, target, reason, null);
        }
    }

    @NexusIncubating(since = "1.3")
    public record ExportLogEntry(String user, String target, Instant createdAt) {
    }

    private NexusAuthoring() {
    }
}
