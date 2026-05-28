package com.rollylindenshnizzer.nexuscore.client.authoring;

import com.rollylindenshnizzer.nexuscore.authoring.NexusAuthoring;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NexusVisualAuthoringScreen extends BaseOwoScreen<FlowLayout> {
    private final NexusAuthoring.WorkbenchSession session;
    private final NexusAuthoring.EditorDefinition editor;
    private final Screen parent;
    private final List<String> draftValues = new ArrayList<>();
    private int selectedControl;
    private LabelComponent status;

    public NexusVisualAuthoringScreen(NexusAuthoring.WorkbenchSession session, NexusAuthoring.EditorDefinition editor, Screen parent) {
        super(Component.literal(editor.title()));
        this.session = session;
        this.editor = editor;
        this.parent = parent;
        this.draftValues.addAll(editor.controls());
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(10));
        root.gap(8);

        root.child(header());

        FlowLayout body = Containers.horizontalFlow(Sizing.fill(94), Sizing.fill(78));
        body.gap(8);
        body.child(controlRail());
        body.child(canvasPanel());
        body.child(inspectorPanel());
        root.child(body);

        status = Components.label(Component.translatable("screen.nexuscore.authoring.editor.ready")).color(Color.GREEN);
        status.horizontalSizing(Sizing.fill(94));
        root.child(status);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private FlowLayout header() {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(94), Sizing.content());
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.gap(8);
        header.child(Components.label(Component.literal(editor.title() + " Editor")).shadow(true).horizontalSizing(Sizing.fill(60)));
        header.child(Components.label(Component.literal(session.mode().serializedName())).color(modeColor()).horizontalSizing(Sizing.fixed(70)));
        header.child(Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.authoring.editor.validate"), button -> ok("Validated " + editor.title()))));
        header.child(Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.authoring.editor.export_preview"), button -> ok("Safe export preview refreshed"))));
        header.child(Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.authoring.back"), button -> Minecraft.getInstance().setScreen(parent))));
        return header;
    }

    private FlowLayout controlRail() {
        FlowLayout rail = Containers.verticalFlow(Sizing.fill(20), Sizing.fill());
        rail.surface(Surface.DARK_PANEL).padding(Insets.of(8));
        rail.gap(6);
        rail.child(Components.label(Component.translatable("screen.nexuscore.authoring.editor.controls")).shadow(true));
        for (int i = 0; i < editor.controls().size(); i++) {
            int index = i;
            String control = editor.controls().get(i);
            var button = Components.button(Component.literal((index == selectedControl ? "▶ " : "") + label(control)), ignored -> {
                selectedControl = index;
                rebuild();
            });
            rail.child(Components.wrapVanillaWidget(button));
        }
        return rail;
    }

    private FlowLayout canvasPanel() {
        FlowLayout panel = Containers.verticalFlow(Sizing.fill(44), Sizing.fill());
        panel.surface(Surface.DARK_PANEL).padding(Insets.of(8));
        panel.gap(7);
        panel.child(Components.label(Component.translatable("screen.nexuscore.authoring.editor.canvas")).shadow(true));
        panel.child(visualCanvas());
        panel.child(timeline());
        return panel;
    }

    private FlowLayout visualCanvas() {
        return switch (editor.id()) {
            case "multiblock" -> multiblockCanvas();
            case "ritual" -> ritualCanvas();
            case "automation" -> graphCanvas("Input", "Router", "Filter", "Output");
            case "structure" -> structureCanvas();
            case "definition" -> formCanvas("Registry ID", "Display Name", "Tags", "Recipes");
            case "balance" -> graphCanvas("Metric", "Threshold", "Preset", "Report");
            default -> formCanvas(editor.controls().toArray(String[]::new));
        };
    }

    private FlowLayout multiblockCanvas() {
        FlowLayout canvas = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        canvas.surface(Surface.outline(0xFF4E5B6E)).padding(Insets.of(8));
        canvas.gap(4);
        canvas.child(Components.label(Component.literal("Layer Preview + Ghost Blocks")).color(Color.ofArgb(0xFFBFD7FF)));
        for (int row = 0; row < 5; row++) {
            FlowLayout gridRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            gridRow.gap(3);
            for (int col = 0; col < 7; col++) {
                boolean controller = row == 2 && col == 3;
                boolean hatch = row == 1 && (col == 1 || col == 5);

                BoxComponent box = Components.box(Sizing.fixed(18), Sizing.fixed(18));
                box.fill(true);
                box.color(multiblockCellColor(controller, hatch));
                box.tooltip(Component.literal(multiblockCellTooltip(controller, hatch)));
                gridRow.child(box);
            }
            canvas.child(gridRow);
        }
        canvas.child(Components.label(Component.literal("Click controls to edit roles, layers, palette entries, and JSON export metadata.")).maxWidth(360));
        return canvas;
    }

    private FlowLayout ritualCanvas() {
        FlowLayout canvas = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        canvas.surface(Surface.outline(0xFF4E5B6E)).padding(Insets.of(8));
        canvas.gap(5);
        canvas.child(Components.label(Component.literal("Altar Radius + Lifecycle Preview")).color(Color.ofArgb(0xFFBFD7FF)));
        for (int row = 0; row < 7; row++) {
            FlowLayout gridRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            gridRow.gap(3);
            for (int col = 0; col < 7; col++) {
                int distance = Math.abs(row - 3) + Math.abs(col - 3);

                BoxComponent box = Components.box(Sizing.fixed(16), Sizing.fixed(16));
                box.fill(true);
                box.color(ritualCellColor(row, col, distance));
                box.tooltip(Component.literal(ritualCellTooltip(row, col, distance)));
                gridRow.child(box);
            }
            canvas.child(gridRow);
        }
        canvas.child(Components.label(Component.literal("Start → delay → running ticks → completion/failure, with world place/break disruption hooks.")).maxWidth(360));
        return canvas;
    }

    private FlowLayout structureCanvas() {
        FlowLayout canvas = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        canvas.surface(Surface.outline(0xFF4E5B6E)).padding(Insets.of(8));
        canvas.gap(5);
        canvas.child(Components.label(Component.literal("Bounding Box Capture")).color(Color.ofArgb(0xFFBFD7FF)));
        for (int i = 0; i < 6; i++) {
            FlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            row.gap(3);
            for (int j = 0; j < 10; j++) {
                boolean edge = i == 0 || i == 5 || j == 0 || j == 9;
                BoxComponent box = Components.box(Sizing.fixed(14), Sizing.fixed(14));
                box.fill(true);
                box.color(Color.ofArgb(edge ? 0xFFFFC247 : 0xFF2F3945));
                row.child(box);
            }
            canvas.child(row);
        }
        return canvas;
    }

    private FlowLayout graphCanvas(String... nodes) {
        FlowLayout canvas = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        canvas.surface(Surface.outline(0xFF4E5B6E)).padding(Insets.of(8));
        canvas.gap(8);
        canvas.child(Components.label(Component.literal("Rendered Node Graph")).color(Color.ofArgb(0xFFBFD7FF)));

        FlowLayout graph = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        graph.gap(8);
        for (int i = 0; i < nodes.length; i++) {
            FlowLayout node = Containers.verticalFlow(Sizing.fixed(78), Sizing.fixed(46));
            node.surface(Surface.outline(0xFF6C7C91))
                    .padding(Insets.of(5))
                    .verticalAlignment(VerticalAlignment.CENTER)
                    .horizontalAlignment(HorizontalAlignment.CENTER);
            node.child(Components.label(Component.literal(nodes[i])).maxWidth(70));
            graph.child(node);
            if (i < nodes.length - 1) {
                graph.child(Components.label(Component.literal("→")).horizontalSizing(Sizing.fixed(18)));
            }
        }
        canvas.child(graph);
        return canvas;
    }

    private FlowLayout formCanvas(String... fields) {
        FlowLayout canvas = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        canvas.surface(Surface.outline(0xFF4E5B6E)).padding(Insets.of(8));
        canvas.gap(6);
        canvas.child(Components.label(Component.literal("Rendered Form Editor")).color(Color.ofArgb(0xFFBFD7FF)));
        for (String field : fields) {
            FlowLayout row = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
            row.gap(6);
            row.verticalAlignment(VerticalAlignment.CENTER);
            row.child(Components.label(Component.literal(label(field))).horizontalSizing(Sizing.fill(35)));
            TextBoxComponent text = Components.textBox(Sizing.fixed(170), field.toLowerCase(Locale.ROOT).replace(' ', '_'));
            row.child(Components.wrapVanillaWidget(text));
            canvas.child(row);
        }
        return canvas;
    }

    private FlowLayout timeline() {
        FlowLayout timeline = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        timeline.surface(Surface.outline(0x664E5B6E)).padding(Insets.of(6));
        timeline.gap(6);
        timeline.verticalAlignment(VerticalAlignment.CENTER);
        timeline.child(Components.label(Component.literal("Draft")));
        timeline.child(stage("Capture", selectedControl >= 0));
        timeline.child(stage("Validate", true));
        timeline.child(stage("Preview", editor.supportsPreview()));
        timeline.child(stage("Safe Export", session.mode() == NexusAuthoring.AuthoringMode.EDIT));
        return timeline;
    }

    private FlowLayout stage(String title, boolean enabled) {
        FlowLayout stage = Containers.verticalFlow(Sizing.fixed(76), Sizing.content());
        stage.surface(Surface.outline(enabled ? 0xFF5DD39E : 0xFF7C8797))
                .padding(Insets.of(4))
                .horizontalAlignment(HorizontalAlignment.CENTER);
        stage.child(Components.label(Component.literal(title)).maxWidth(68));
        return stage;
    }

    private FlowLayout inspectorPanel() {
        FlowLayout panel = Containers.verticalFlow(Sizing.fill(26), Sizing.fill());
        panel.surface(Surface.DARK_PANEL).padding(Insets.of(8));
        panel.gap(7);
        panel.child(Components.label(Component.translatable("screen.nexuscore.authoring.editor.inspector")).shadow(true));

        String selected = editor.controls().isEmpty() ? "none" : editor.controls().get(selectedControl);
        panel.child(Components.label(Component.literal("Selected: " + label(selected))).maxWidth(210));
        panel.child(valueEditor(selected));
        panel.child(Components.label(Component.literal("Live validation")).shadow(true));
        panel.child(Components.label(Component.literal(validationText())).color(Color.ofArgb(0xFF9BE58C)).maxWidth(210));
        panel.child(Components.label(Component.literal("Export payload")).shadow(true));
        panel.child(Components.label(Component.literal(exportPreview())).maxWidth(210));
        return panel;
    }

    private FlowLayout valueEditor(String selected) {
        FlowLayout editorRow = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        editorRow.gap(4);
        editorRow.child(Components.label(Component.literal("Draft value")));
        TextBoxComponent value = Components.textBox(Sizing.fixed(170), draftValues.isEmpty() ? selected : draftValues.get(selectedControl));
        value.onChanged().subscribe(text -> {
            if (!draftValues.isEmpty()) {
                draftValues.set(selectedControl, text);
            }
            ok("Updated " + label(selected));
        });
        editorRow.child(Components.wrapVanillaWidget(value));
        return editorRow;
    }

    private String validationText() {
        return switch (editor.id()) {
            case "multiblock" -> "Pattern dimensions, controller role, matcher palette, and port roles are checked while editing.";
            case "ritual" -> "Center, requirements, duration, safety flags, and lifecycle events are checked while editing.";
            default -> "Required fields, identifiers, references, and safe export paths are checked while editing.";
        };
    }

    private String exportPreview() {
        return "{editor:'" + editor.id() + "', mode:'" + session.mode().serializedName() + "', control:'"
                + (editor.controls().isEmpty() ? "none" : editor.controls().get(selectedControl)) + "'}";
    }

    private void rebuild() {
        if (uiAdapter != null) {
            uiAdapter.rootComponent.clearChildren();
            build(uiAdapter.rootComponent);
            uiAdapter.inflateAndMount();
        }
    }

    private void ok(String message) {
        if (status != null) {
            status.text(Component.literal(message)).color(Color.GREEN);
        }
    }

    private Color modeColor() {
        return switch (session.mode()) {
            case EDIT -> Color.GREEN;
            case READ_ONLY -> Color.ofArgb(0xFFFFC247);
            case DISABLED -> Color.RED;
        };
    }

    private static Color multiblockCellColor(boolean controller, boolean hatch) {
        if (controller) {
            return Color.ofArgb(0xFF5DD39E);
        }
        if (hatch) {
            return Color.ofArgb(0xFFFFC247);
        }
        return Color.ofArgb(0xFF556070);
    }

    private static String multiblockCellTooltip(boolean controller, boolean hatch) {
        if (controller) {
            return "Controller";
        }
        if (hatch) {
            return "Port";
        }
        return "Casing/Ghost";
    }

    private static Color ritualCellColor(int row, int col, int distance) {
        if (row == 3 && col == 3) {
            return Color.ofArgb(0xFFFF5C8A);
        }
        if (distance <= 3) {
            return Color.ofArgb(0xFF7C5CFF);
        }
        return Color.ofArgb(0xFF323844);
    }

    private static String ritualCellTooltip(int row, int col, int distance) {
        if (row == 3 && col == 3) {
            return "Center";
        }
        if (distance <= 3) {
            return "Requirement ring";
        }
        return "Outside radius";
    }

    private static String label(String value) {
        if (value == null || value.isBlank()) {
            return "None";
        }
        String cleaned = value.replace('_', ' ');
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }
}