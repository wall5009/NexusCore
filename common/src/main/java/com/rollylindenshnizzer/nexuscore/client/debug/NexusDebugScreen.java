package com.rollylindenshnizzer.nexuscore.client.debug;

import com.rollylindenshnizzer.nexuscore.authoring.NexusAuthoring;
import com.rollylindenshnizzer.nexuscore.client.NexusClientDescriptors;
import com.rollylindenshnizzer.nexuscore.client.authoring.NexusAuthoringScreens;
import com.rollylindenshnizzer.nexuscore.client.machine.NexusMachineScreens;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import com.rollylindenshnizzer.nexuscore.debug.NexusDoctor;
import com.rollylindenshnizzer.nexuscore.event.EventTrace;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import com.rollylindenshnizzer.nexuscore.world.NexusWorldEventHooks;
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
                .padding(Insets.of(10));
        root.gap(8);

        FlowLayout header = Containers.verticalFlow(Sizing.fill(94), Sizing.content());
        header.gap(6).horizontalAlignment(HorizontalAlignment.CENTER);
        header.child(Components.label(Component.translatable("screen.nexuscore.debug.title")).shadow(true).horizontalSizing(Sizing.content()));

        FlowLayout actions = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        actions.gap(6).verticalAlignment(VerticalAlignment.CENTER);
        actions.child(Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.debug.open_machine_preview"),
                button -> NexusMachineScreens.openFirstPreview(this))));
        actions.child(Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.debug.open_authoring"),
                button -> NexusAuthoringScreens.openWorkbench(this))));
        header.child(actions);
        root.child(header);

        FlowLayout body = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        body.gap(8);
        body.child(section("screen.nexuscore.debug.sections", debugSections()));
        body.child(section("screen.nexuscore.debug.events", eventSections()));
        body.child(section("screen.nexuscore.debug.authoring", authoringSections()));
        body.child(section("screen.nexuscore.debug.world_hooks", worldHookSections()));
        body.child(section("screen.nexuscore.debug.client", clientSections()));
        body.child(section("screen.nexuscore.debug.doctor", doctorSections()));
        body.child(section("screen.nexuscore.debug.network", networkSections()));
        root.child(Containers.verticalScroll(Sizing.fill(94), Sizing.fill(84), body));

        root.child(Components.label(Component.translatable("screen.nexuscore.debug.close_hint")));
    }

    private FlowLayout section(String titleKey, Iterable<String> lines) {
        FlowLayout content = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        content.surface(Surface.DARK_PANEL)
                .padding(Insets.of(8))
                .horizontalAlignment(HorizontalAlignment.LEFT);
        content.gap(4);
        content.child(Components.label(Component.translatable(titleKey)).shadow(true));
        for (String line : lines) {
            content.child(Components.label(Component.literal(line)).maxWidth(640));
        }
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

    private Iterable<String> authoringSections() {
        return NexusAuthoring.editors().stream()
                .map(editor -> editor.id() + " (" + editor.kind() + ") controls=" + editor.controls().size())
                .toList();
    }

    private Iterable<String> worldHookSections() {
        return NexusWorldEventHooks.hooks().stream()
                .map(hook -> hook.name() + ": " + hook.status() + " " + hook.detail())
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
