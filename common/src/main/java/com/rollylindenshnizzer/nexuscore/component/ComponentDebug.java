package com.rollylindenshnizzer.nexuscore.component;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ComponentDebug {
    public static String prettyPrint(ItemStack stack) {
        StringBuilder builder = new StringBuilder(stack.getHoverName().getString()).append('\n');
        DataComponentMap components = stack.getComponents();
        components.forEach(component -> builder.append(" - ")
                .append(component.type())
                .append(" = ")
                .append(component.value())
                .append('\n'));
        return builder.toString();
    }

    public static Map<DataComponentType<?>, Change> diff(ItemStack before, ItemStack after) {
        Map<DataComponentType<?>, Change> changes = new LinkedHashMap<>();
        before.getComponents().forEach(component -> {
            Object next = after.get(component.type());
            if (!Objects.equals(component.value(), next)) {
                changes.put(component.type(), new Change(component.value(), next));
            }
        });
        after.getComponents().forEach(component -> {
            if (before.get(component.type()) == null) {
                changes.put(component.type(), new Change(null, component.value()));
            }
        });
        return changes;
    }

    public record Change(Object before, Object after) {
    }

    private ComponentDebug() {
    }
}
