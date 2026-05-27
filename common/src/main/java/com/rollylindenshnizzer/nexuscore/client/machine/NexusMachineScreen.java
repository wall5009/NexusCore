package com.rollylindenshnizzer.nexuscore.client.machine;

import com.rollylindenshnizzer.nexuscore.energy.NexusEnergyStorage;
import com.rollylindenshnizzer.nexuscore.fluid.NexusFluidTank;
import com.rollylindenshnizzer.nexuscore.inventory.SimpleItemHandler;
import com.rollylindenshnizzer.nexuscore.inventory.SlotGroup;
import com.rollylindenshnizzer.nexuscore.machine.MachineScreenLayout;
import com.rollylindenshnizzer.nexuscore.machine.MachineState;
import com.rollylindenshnizzer.nexuscore.machine.NexusMachineDefinition;
import com.rollylindenshnizzer.nexuscore.machine.RedstoneControlMode;
import com.rollylindenshnizzer.nexuscore.machine.SideConfiguration;
import com.rollylindenshnizzer.nexuscore.ui.WidgetDescriptor;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class NexusMachineScreen extends BaseOwoScreen<FlowLayout> {
    private final NexusMachineDefinition definition;
    private final MachineScreenLayout layout;
    private final MachineState state;
    private final NexusEnergyStorage energy;
    private final NexusFluidTank fluid;
    private final SimpleItemHandler inventory;
    private final Screen parent;
    private final EnumMap<Direction, SideConfiguration> sideConfiguration = new EnumMap<>(Direction.class);
    private int rebuildCooldown;

    public NexusMachineScreen(NexusMachineDefinition definition,
                              MachineScreenLayout layout,
                              MachineState state,
                              NexusEnergyStorage energy,
                              NexusFluidTank fluid,
                              SimpleItemHandler inventory,
                              Screen parent) {
        super(Component.literal(definition.id().toString()));
        this.definition = definition;
        this.layout = layout == null ? MachineScreenLayout.generated(definition) : layout;
        this.state = state;
        this.energy = energy;
        this.fluid = fluid;
        this.inventory = inventory;
        this.parent = parent;
        for (Direction direction : Direction.values()) {
            sideConfiguration.put(direction, definition.side(direction));
        }
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

        FlowLayout header = Containers.horizontalFlow(Sizing.fill(92), Sizing.content());
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.gap(8);
        header.child(Components.label(Component.literal(definition.id().toString())).shadow(true).horizontalSizing(Sizing.fill(70)));
        header.child(Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.machine.done"),
                button -> Minecraft.getInstance().setScreen(parent))));
        root.child(header);

        FlowLayout body = Containers.horizontalFlow(Sizing.fill(92), Sizing.fill(82));
        body.gap(8);

        FlowLayout controls = Containers.verticalFlow(Sizing.fill(44), Sizing.fill());
        controls.surface(Surface.DARK_PANEL).padding(Insets.of(8));
        controls.gap(7);
        controls.child(Components.label(Component.translatable("screen.nexuscore.machine.controls")).shadow(true));
        for (WidgetDescriptor widget : layout.widgets()) {
            controls.child(renderWidget(widget));
        }
        body.child(controls);

        FlowLayout inventoryPanel = Containers.verticalFlow(Sizing.fill(48), Sizing.fill());
        inventoryPanel.surface(Surface.DARK_PANEL).padding(Insets.of(8));
        inventoryPanel.gap(7);
        inventoryPanel.child(Components.label(Component.translatable("screen.nexuscore.machine.inventory")).shadow(true));
        for (SlotGroup group : definition.slotGroups()) {
            inventoryPanel.child(slotGroup(group));
        }
        body.child(inventoryPanel);
        root.child(body);

        root.child(Components.label(Component.literal(state.status() + " " + Math.round(state.progressFraction() * 100.0F) + "%"))
                .horizontalSizing(Sizing.fill(92)));
    }

    @Override
    public void tick() {
        super.tick();
        if (++rebuildCooldown >= 10) {
            rebuildCooldown = 0;
            rebuild();
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private io.wispforest.owo.ui.core.Component renderWidget(WidgetDescriptor widget) {
        return switch (widget.type()) {
            case "progress_arrow" -> progressWidget(widget);
            case "energy_bar" -> energyWidget(widget);
            case "fluid_tank" -> fluidWidget(widget);
            case "redstone_mode" -> redstoneWidget();
            case "side_config" -> sideConfigWidget();
            case "item_slot" -> slotWidget(widget);
            case "label", "text" -> textWidget(widget);
            case "button" -> buttonWidget(widget);
            default -> fallbackWidget(widget);
        };
    }

    private FlowLayout progressWidget(WidgetDescriptor widget) {
        int width = intProperty(widget, "width", 116);
        int filled = Math.max(1, Math.round(width * state.progressFraction()));
        FlowLayout row = labeledRow(label(widget, "Progress"));
        FlowLayout bar = bar(width, 12, filled, 0xFF4AA3FF, 0xFF1C2632);
        bar.tooltip(Component.literal(state.progress() + " / " + state.maxProgress()));
        row.child(bar);
        return row;
    }

    private FlowLayout energyWidget(WidgetDescriptor widget) {
        int width = intProperty(widget, "width", 116);
        int filled = energy.capacity() == 0 ? 1 : Math.max(1, (int) Math.round(width * (energy.amount() / (double) energy.capacity())));
        FlowLayout row = labeledRow(label(widget, "Energy"));
        FlowLayout bar = bar(width, 12, filled, 0xFFFFC247, 0xFF2D2615);
        bar.tooltip(Component.literal(energy.amount() + " / " + energy.capacity() + " FE"));
        row.child(bar);
        return row;
    }

    private FlowLayout fluidWidget(WidgetDescriptor widget) {
        int height = intProperty(widget, "height", 48);
        int filled = fluid.capacity() == 0 ? 1 : Math.max(1, (int) Math.round(height * (fluid.stored().amount() / (double) fluid.capacity())));
        FlowLayout row = labeledRow(label(widget, "Fluid"));
        FlowLayout tank = Containers.verticalFlow(Sizing.fixed(22), Sizing.fixed(height));
        tank.surface(Surface.outline(0xFF7A8797)).verticalAlignment(VerticalAlignment.BOTTOM).padding(Insets.of(2));
        tank.child(Components.box(Sizing.fill(), Sizing.fixed(filled)).fill(true).color(Color.ofArgb(0xFF3D8DFF)));
        tank.tooltip(Component.literal(fluid.stored().amount() + " / " + fluid.capacity() + " mB"));
        row.child(tank);
        return row;
    }

    private FlowLayout redstoneWidget() {
        FlowLayout row = labeledRow("Redstone");
        row.child(Components.wrapVanillaWidget(Components.button(Component.literal(state.redstoneMode().name()), button -> {
            RedstoneControlMode[] values = RedstoneControlMode.values();
            state.redstoneMode(values[(state.redstoneMode().ordinal() + 1) % values.length]);
            rebuild();
        })).horizontalSizing(Sizing.fixed(116)));
        return row;
    }

    private FlowLayout sideConfigWidget() {
        FlowLayout wrapper = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        wrapper.gap(4);
        wrapper.child(Components.label(Component.literal("Sides")).shadow(true));
        FlowLayout grid = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        grid.gap(4);
        for (Direction direction : Direction.values()) {
            grid.child(Components.wrapVanillaWidget(Components.button(Component.literal(direction.getName().substring(0, 1).toUpperCase() + ":" + sideConfiguration.get(direction).name().substring(0, 1)),
                    button -> {
                        sideConfiguration.put(direction, cycle(sideConfiguration.get(direction)));
                        rebuild();
                    })).horizontalSizing(Sizing.fixed(38)).tooltip(Component.literal(direction.getName() + " " + sideConfiguration.get(direction))));
        }
        wrapper.child(grid);
        return wrapper;
    }

    private FlowLayout slotWidget(WidgetDescriptor widget) {
        int slot = intProperty(widget, "slot", 0);
        FlowLayout row = labeledRow(label(widget, "Slot " + slot));
        row.child(itemBox(slot));
        return row;
    }

    private FlowLayout textWidget(WidgetDescriptor widget) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        row.child(Components.label(Component.literal(widget.properties().getOrDefault("text", widget.binding()))).maxWidth(220));
        return row;
    }

    private FlowLayout buttonWidget(WidgetDescriptor widget) {
        FlowLayout row = labeledRow(label(widget, widget.binding()));
        row.child(Components.wrapVanillaWidget(Components.button(Component.literal(widget.properties().getOrDefault("text", "Run")),
                button -> rebuild())).horizontalSizing(Sizing.fixed(116)));
        return row;
    }

    private FlowLayout fallbackWidget(WidgetDescriptor widget) {
        FlowLayout row = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        row.surface(Surface.outline(0xFF596575)).padding(Insets.of(5));
        row.child(Components.label(Component.literal(widget.type() + " -> " + widget.binding())).maxWidth(220));
        return row;
    }

    private FlowLayout slotGroup(SlotGroup group) {
        FlowLayout section = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        section.gap(4);
        section.child(Components.label(Component.literal(group.name() + " [" + group.role() + "]")).shadow(true));
        FlowLayout slots = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        slots.gap(4);
        List<Integer> slotIds = new ArrayList<>();
        group.range().stream().forEach(slotIds::add);
        for (int slot : slotIds) {
            slots.child(itemBox(slot));
        }
        section.child(slots);
        return section;
    }

    private FlowLayout itemBox(int slot) {
        ItemStack stack = slot >= 0 && slot < inventory.size() ? inventory.get(slot) : ItemStack.EMPTY;
        FlowLayout box = Containers.verticalFlow(Sizing.fixed(24), Sizing.fixed(24));
        box.surface(Surface.PANEL_INSET)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        box.child(Components.item(stack));
        box.tooltip(Component.literal("Slot " + slot + ": " + stack.getCount() + "x " + stack.getHoverName().getString()));
        return box;
    }

    private FlowLayout labeledRow(String label) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.gap(6);
        row.child(Components.label(Component.literal(label)).horizontalSizing(Sizing.fixed(78)));
        return row;
    }

    private FlowLayout bar(int width, int height, int filled, int color, int background) {
        FlowLayout outer = Containers.horizontalFlow(Sizing.fixed(width), Sizing.fixed(height));
        outer.surface(Surface.flat(background).and(Surface.outline(0xFF7A8797)));
        BoxComponent inner = Components.box(Sizing.fixed(Math.min(width, filled)), Sizing.fill())
                .fill(true)
                .color(Color.ofArgb(color));
        outer.child(inner);
        return outer;
    }

    private void rebuild() {
        if (uiAdapter != null) {
            uiAdapter.rootComponent.clearChildren();
            build(uiAdapter.rootComponent);
            uiAdapter.inflateAndMount();
        }
    }

    private static SideConfiguration cycle(SideConfiguration configuration) {
        SideConfiguration[] values = SideConfiguration.values();
        return values[(configuration.ordinal() + 1) % values.length];
    }

    private static int intProperty(WidgetDescriptor widget, String key, int fallback) {
        try {
            return Integer.parseInt(widget.properties().getOrDefault(key, Integer.toString(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String label(WidgetDescriptor widget, String fallback) {
        return widget.properties().getOrDefault("label", fallback);
    }
}
