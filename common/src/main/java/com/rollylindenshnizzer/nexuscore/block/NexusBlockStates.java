package com.rollylindenshnizzer.nexuscore.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class NexusBlockStates {
    public static BooleanProperty bool(String name) {
        return BooleanProperty.create(name);
    }

    public static IntegerProperty integer(String name, int min, int max) {
        return IntegerProperty.create(name, min, max);
    }

    public static <E extends Enum<E> & StringRepresentable> EnumProperty<E> enumProperty(String name, Class<E> enumClass) {
        return EnumProperty.create(name, enumClass);
    }

    public static DirectionProperty direction(String name) {
        return DirectionProperty.create(name);
    }

    public static <T extends Comparable<T>> T require(Property<T> property, String value) {
        return property.getValue(value).orElseThrow(() -> new IllegalArgumentException("Invalid value '" + value + "' for property " + property.getName()));
    }

    private NexusBlockStates() {
    }
}
