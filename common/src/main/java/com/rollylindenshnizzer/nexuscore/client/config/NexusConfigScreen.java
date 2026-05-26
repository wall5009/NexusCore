package com.rollylindenshnizzer.nexuscore.client.config;

import com.rollylindenshnizzer.nexuscore.config.BooleanOption;
import com.rollylindenshnizzer.nexuscore.config.ConfigOption;
import com.rollylindenshnizzer.nexuscore.config.EnumOption;
import com.rollylindenshnizzer.nexuscore.config.IntOption;
import com.rollylindenshnizzer.nexuscore.config.NexusConfig;
import com.rollylindenshnizzer.nexuscore.config.StringOption;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
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

import java.util.Arrays;
import java.util.OptionalInt;

public final class NexusConfigScreen extends BaseOwoScreen<FlowLayout> {
    private final NexusConfig config;
    private final Screen parent;
    private LabelComponent status;

    public NexusConfigScreen(NexusConfig config, Screen parent) {
        super(Component.translatable("screen.nexuscore.config", config.modId()));
        this.config = config;
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
                .padding(Insets.of(12));
        root.gap(8);

        FlowLayout header = Containers.horizontalFlow(Sizing.fill(92), Sizing.content());
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.gap(8);
        header.child(Components.label(Component.translatable("screen.nexuscore.config.title", config.modId()))
                .shadow(true)
                .horizontalSizing(Sizing.fill(80)));
        header.child(Components.wrapVanillaWidget(Components.button(
                Component.translatable("screen.nexuscore.config.reset_all"), button -> resetAll())));
        header.child(Components.wrapVanillaWidget(Components.button(
                Component.translatable("screen.nexuscore.config.done"), button -> Minecraft.getInstance().setScreen(parent))));
        root.child(header);

        FlowLayout list = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        list.gap(6);
        for (ConfigOption<?> option : config.options().values()) {
            if (option.visible()) {
                list.child(row(option));
            }
        }
        root.child(Containers.verticalScroll(Sizing.fill(92), Sizing.fill(86), list));

        status = Components.label(Component.translatable("screen.nexuscore.config.ready")).color(Color.GREEN);
        status.horizontalSizing(Sizing.fill(92));
        root.child(status);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private FlowLayout row(ConfigOption<?> option) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        row.surface(Surface.DARK_PANEL)
                .padding(Insets.of(6))
                .verticalAlignment(VerticalAlignment.CENTER);
        row.gap(8);

        FlowLayout labels = Containers.verticalFlow(Sizing.fill(45), Sizing.content());
        labels.gap(2);
        labels.child(Components.label(Component.literal(option.key())).shadow(true));
        if (!option.comment().isBlank()) {
            labels.child(Components.label(Component.literal(option.comment())).maxWidth(240));
        }
        labels.child(Components.label(flags(option)).maxWidth(240));
        row.child(labels);

        row.child(editor(option));
        var reset = Components.wrapVanillaWidget(Components.button(Component.translatable("screen.nexuscore.config.reset"),
                button -> {
                    option.reset();
                    rebuild();
                    ok(Component.translatable("screen.nexuscore.config.reset_status", option.key()));
                }));
        reset.horizontalSizing(Sizing.fixed(60));
        row.child(reset);
        return row;
    }

    private io.wispforest.owo.ui.core.Component editor(ConfigOption<?> option) {
        if (option instanceof BooleanOption booleanOption) {
            return Components.wrapVanillaWidget(Components.checkbox(Component.empty())
                    .checked(booleanOption.get())
                    .onChanged(value -> set(booleanOption, value)));
        }
        if (option instanceof IntOption intOption) {
            return intEditor(intOption);
        }
        if (option instanceof StringOption stringOption) {
            TextBoxComponent text = Components.textBox(Sizing.fixed(150), stringOption.get());
            text.onChanged().subscribe(value -> set(stringOption, value));
            return Components.wrapVanillaWidget(text);
        }
        if (option instanceof EnumOption<?> enumOption) {
            return enumEditor(enumOption);
        }
        return Components.label(Component.literal(String.valueOf(option.get()))).horizontalSizing(Sizing.fixed(150));
    }

    private FlowLayout intEditor(IntOption option) {
        FlowLayout editor = Containers.horizontalFlow(Sizing.fixed(210), Sizing.content());
        editor.verticalAlignment(VerticalAlignment.CENTER);
        editor.gap(4);
        TextBoxComponent text = Components.textBox(Sizing.fixed(62), Integer.toString(option.get()));
        text.onChanged().subscribe(value -> {
            try {
                set(option, Integer.parseInt(value.trim()));
            } catch (NumberFormatException exception) {
                error(Component.translatable("screen.nexuscore.config.invalid_integer", option.key()));
            }
        });
        editor.child(Components.wrapVanillaWidget(text));

        OptionalInt min = option.min();
        OptionalInt max = option.max();
        if (min.isPresent() && max.isPresent()) {
            var slider = Components.discreteSlider(Sizing.fixed(130), min.getAsInt(), max.getAsInt())
                    .decimalPlaces(0)
                    .snap(true)
                    .setFromDiscreteValue(option.get());
            slider.onChanged().subscribe(value -> {
                int rounded = (int) Math.round(value);
                text.text(Integer.toString(rounded));
                set(option, rounded);
            });
            editor.child(Components.wrapVanillaWidget(slider));
        }
        return editor;
    }

    private io.wispforest.owo.ui.core.Component enumEditor(EnumOption<?> option) {
        ButtonComponent button = Components.button(Component.literal(option.get().name()), ignored -> {
            cycleEnum(option);
            rebuild();
            ok(Component.translatable("screen.nexuscore.config.updated", option.key()));
        });
        var wrapped = Components.wrapVanillaWidget(button);
        wrapped.horizontalSizing(Sizing.fixed(150));
        return wrapped;
    }

    private void resetAll() {
        for (ConfigOption<?> option : config.options().values()) {
            option.reset();
        }
        rebuild();
        ok(Component.translatable("screen.nexuscore.config.reset_all_status"));
    }

    private void rebuild() {
        if (uiAdapter != null) {
            uiAdapter.rootComponent.clearChildren();
            build(uiAdapter.rootComponent);
            uiAdapter.inflateAndMount();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void cycleEnum(EnumOption option) {
        Enum<?> current = (Enum<?>) option.get();
        Object[] values = option.enumClass().getEnumConstants();
        int next = (current.ordinal() + 1) % values.length;
        option.set(values[next]);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void set(ConfigOption option, Object value) {
        try {
            option.set(value);
            ok(Component.translatable("screen.nexuscore.config.updated", option.key()));
        } catch (RuntimeException exception) {
            error(Component.literal(option.key() + ": " + exception.getMessage()));
        }
    }

    private Component flags(ConfigOption<?> option) {
        return Component.literal(String.join(", ", Arrays.asList(
                        option.restartRequired() ? "restart" : "",
                        option.worldReloadRequired() ? "world reload" : "",
                        option.isServerSynced() ? "server synced" : "")
                .stream()
                .filter(flag -> !flag.isBlank())
                .toList()));
    }

    private void ok(Component message) {
        if (status != null) {
            status.text(message).color(Color.GREEN);
        }
    }

    private void error(Component message) {
        if (status != null) {
            status.text(message).color(Color.RED);
        }
    }
}
