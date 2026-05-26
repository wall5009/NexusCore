package com.rollylindenshnizzer.nexuscore.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;

public final class DamageHelpers {
    public static List<LivingEntity> livingInBox(Entity source, AABB box, Predicate<LivingEntity> predicate) {
        return source.level().getEntitiesOfClass(LivingEntity.class, box, predicate);
    }

    public static boolean friendlyFireAllowed(Entity attacker, Entity target) {
        return attacker.getTeam() == null || target.getTeam() == null || attacker.getTeam().isAllowFriendlyFire() || !attacker.isAlliedTo(target);
    }

    private DamageHelpers() {
    }
}
