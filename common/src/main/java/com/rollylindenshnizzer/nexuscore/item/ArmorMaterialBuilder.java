package com.rollylindenshnizzer.nexuscore.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ArmorMaterialBuilder {
    private final ResourceLocation layerId;
    private final Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
    private int enchantmentValue;
    private Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_GENERIC;
    private Supplier<Ingredient> repairIngredient = () -> Ingredient.EMPTY;
    private float toughness;
    private float knockbackResistance;
    private boolean dyeable;

    public ArmorMaterialBuilder(ResourceLocation layerId) {
        this.layerId = layerId;
    }

    public ArmorMaterialBuilder defense(ArmorItem.Type type, int value) {
        defense.put(type, value);
        return this;
    }

    public ArmorMaterialBuilder enchantmentValue(int enchantmentValue) {
        this.enchantmentValue = enchantmentValue;
        return this;
    }

    public ArmorMaterialBuilder equipSound(Holder<SoundEvent> equipSound) {
        this.equipSound = equipSound;
        return this;
    }

    public ArmorMaterialBuilder repairIngredient(Supplier<Ingredient> repairIngredient) {
        this.repairIngredient = repairIngredient;
        return this;
    }

    public ArmorMaterialBuilder toughness(float toughness) {
        this.toughness = toughness;
        return this;
    }

    public ArmorMaterialBuilder knockbackResistance(float knockbackResistance) {
        this.knockbackResistance = knockbackResistance;
        return this;
    }

    public ArmorMaterialBuilder dyeable() {
        this.dyeable = true;
        return this;
    }

    public ArmorMaterial build() {
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            defense.putIfAbsent(type, 0);
        }
        ArmorMaterial.Layer layer = dyeable
                ? new ArmorMaterial.Layer(layerId, "", true)
                : new ArmorMaterial.Layer(layerId);
        return new ArmorMaterial(Map.copyOf(defense), enchantmentValue, equipSound, repairIngredient,
                List.of(layer), toughness, knockbackResistance);
    }
}
