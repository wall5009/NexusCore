package com.rollylindenshnizzer.nexuscore.entity;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Map;

@NexusStable(since = "1.2")
public record CombatProfile(ResourceLocation id,
                            double attackDamage,
                            double attackSpeed,
                            double armor,
                            double armorToughness,
                            double knockbackResistance,
                            Map<EquipmentSlot, ResourceLocation> equipmentLootTables) {
    public CombatProfile {
        attackDamage = Math.max(0.0, attackDamage);
        attackSpeed = Math.max(0.0, attackSpeed);
        armor = Math.max(0.0, armor);
        armorToughness = Math.max(0.0, armorToughness);
        knockbackResistance = Math.max(0.0, Math.min(1.0, knockbackResistance));
        equipmentLootTables = equipmentLootTables == null ? Map.of() : Map.copyOf(equipmentLootTables);
    }
}
