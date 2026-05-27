package com.rollylindenshnizzer.nexuscore.client.debug;

import com.rollylindenshnizzer.nexuscore.client.NexusClientDescriptors;
import com.rollylindenshnizzer.nexuscore.client.machine.NexusMachineScreens;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import com.rollylindenshnizzer.nexuscore.debug.NexusDoctor;
import com.rollylindenshnizzer.nexuscore.event.EventTrace;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;

import java.util.Map;

public final class NexusDebugScreen extends BaseOwoScreen<FlowLayout> {
    public NexusDebugScreen() {
        super(Component.translatable("screen.nexuscore.debug"));
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
                .padding(Insets.of(12));
        root.gap(8);

        root.child(Components.label(Component.translatable("screen.nexuscore.debug.title"))
                .shadow(true)
                .horizontalSizing(Sizing.content()));
        root.child(Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.debug.open_machine_preview"),
                button -> NexusMachineScreens.openFirstPreview(this))));

        FlowLayout columns = Containers.horizontalFlow(Sizing.fill(92), Sizing.fill(88));
        columns.gap(8);
        columns.child(section("screen.nexuscore.debug.sections", debugSections()));
        columns.child(section("screen.nexuscore.debug.events", eventSections()));
        columns.child(section("screen.nexuscore.debug.client", clientSections()));
        root.child(columns);

        FlowLayout diagnostics = Containers.horizontalFlow(Sizing.fill(92), Sizing.fill(40));
        diagnostics.gap(8);
        diagnostics.child(section("screen.nexuscore.debug.doctor", doctorSections(), 46));
        diagnostics.child(section("screen.nexuscore.debug.network", networkSections(), 46));
        root.child(diagnostics);

        root.child(Components.label(Component.translatable("screen.nexuscore.debug.close_hint")));
    }

    private FlowLayout section(String titleKey, Iterable<String> lines) {
        return section(titleKey, lines, 33);
    }

    private FlowLayout section(String titleKey, Iterable<String> lines, int widthPercent) {
        FlowLayout content = Containers.verticalFlow(Sizing.fill(widthPercent), Sizing.fill());
        content.surface(Surface.DARK_PANEL)
                .padding(Insets.of(8))
                .horizontalAlignment(HorizontalAlignment.LEFT);
        content.gap(4);
        content.child(Components.label(Component.translatable(titleKey)).shadow(true));
        FlowLayout list = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        list.gap(3);
        for (String line : lines) {
            list.child(Components.label(Component.literal(line)).maxWidth(220));
        }
        content.child(Containers.verticalScroll(Sizing.fill(), Sizing.fill(), list));
        return content;
    }

    private Iterable<String> debugSections() {
        return DebugRegistry.snapshot().entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .toList();
    }

    private Iterable<String> eventSections() {
        return EventTrace.entries().stream()
                .map(entry -> entry.event() + ": " + entry.detail())
                .toList();
    }

    private Iterable<String> clientSections() {
        return NexusClientDescriptors.descriptors().stream()
                .map(descriptor -> descriptor.id() + " (" + descriptor.getClass().getSimpleName() + ")")
                .toList();
    }

    private Iterable<String> doctorSections() {
        return NexusDoctor.run("nexuscore").issues().stream()
                .map(issue -> issue.severity() + " " + issue.code() + ": " + issue.title())
                .toList();
    }

    private Iterable<String> networkSections() {
        return NexusNetworking.diagnostics().entrySet().stream()
                .map(entry -> entry.getKey() + " v" + entry.getValue().protocolVersion()
                        + " packets=" + entry.getValue().packets().size())
                .toList();
    }
}
