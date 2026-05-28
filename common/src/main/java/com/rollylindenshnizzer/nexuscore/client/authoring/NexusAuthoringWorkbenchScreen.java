package com.rollylindenshnizzer.nexuscore.client.authoring;

import com.rollylindenshnizzer.nexuscore.authoring.NexusAuthoring;
import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblocks;
import com.rollylindenshnizzer.nexuscore.ritual.NexusRituals;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
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

public final class NexusAuthoringWorkbenchScreen extends BaseOwoScreen<FlowLayout> {
    private final NexusAuthoring.WorkbenchSession session;
    private final Screen parent;

    public NexusAuthoringWorkbenchScreen(NexusAuthoring.WorkbenchSession session, Screen parent) {
        super(Component.translatable("screen.nexuscore.authoring"));
        this.session = session;
        this.parent = parent;
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

        FlowLayout header = Containers.horizontalFlow(Sizing.fill(94), Sizing.content());
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.child(Components.label(Component.translatable("screen.nexuscore.authoring.title")).shadow(true).horizontalSizing(Sizing.fill(70)));
        header.child(Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.authoring.done"), button -> Minecraft.getInstance().setScreen(parent))));
        root.child(header);

        FlowLayout body = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        body.child(sessionPanel());
        body.child(editorList());
        body.child(runtimePanel());
        root.child(Containers.verticalScroll(Sizing.fill(94), Sizing.fill(86), body));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private FlowLayout sessionPanel() {
        FlowLayout panel = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        panel.surface(Surface.DARK_PANEL).padding(Insets.of(8));
        panel.child(Components.label(Component.translatable("screen.nexuscore.authoring.session")).shadow(true));
        panel.child(Components.label(Component.literal("user=" + session.user() + ", mode=" + session.mode().serializedName())).maxWidth(520));
        panel.child(Components.label(Component.literal("development=" + session.developmentMode() + ", server=" + session.serverEnabled() + ", opened=" + session.openedAt())).maxWidth(520));
        return panel;
    }

    private FlowLayout editorList() {
        FlowLayout list = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        list.surface(Surface.DARK_PANEL).padding(Insets.of(8));
        list.child(Components.label(Component.translatable("screen.nexuscore.authoring.editors")).shadow(true));
        for (NexusAuthoring.EditorDefinition editor : session.editors()) {
            list.child(editorRow(editor));
        }
        return list;
    }

    private FlowLayout editorRow(NexusAuthoring.EditorDefinition editor) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        row.surface(Surface.outline(0x663C4654));
        row.padding(Insets.of(6));
        row.verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout text = Containers.verticalFlow(Sizing.fill(68), Sizing.content());
        text.child(Components.label(Component.literal(editor.title() + "  [" + editor.kind().name().toLowerCase() + "]")).shadow(true));
        text.child(Components.label(Component.literal(String.join("  •  ", editor.controls()))).maxWidth(460));
        text.child(Components.label(Component.literal(flags(editor))).color(Color.ofArgb(0xFF9FB6C8)).maxWidth(460));
        row.child(text);

        row.child(Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.authoring.open_editor"),
                button -> NexusAuthoringScreens.openEditor(session, editor, this))));
        return row;
    }

    private FlowLayout runtimePanel() {
        FlowLayout panel = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        panel.surface(Surface.DARK_PANEL).padding(Insets.of(8));
        panel.child(Components.label(Component.translatable("screen.nexuscore.authoring.runtime")).shadow(true));
        panel.child(Components.label(Component.literal(NexusAuthoring.debugSummary())).maxWidth(520));
        panel.child(Components.label(Component.literal(NexusMultiblocks.debugSummary())).maxWidth(520));
        panel.child(Components.label(Component.literal(NexusRituals.debugSummary())).maxWidth(520));
        return panel;
    }

    private static String flags(NexusAuthoring.EditorDefinition editor) {
        return "preview=" + editor.supportsPreview()
                + ", clipboard=" + editor.supportsClipboard()
                + ", safeReload=" + editor.supportsSafeReload()
                + ", validatesLive=" + editor.validatesWhileEditing();
    }
}