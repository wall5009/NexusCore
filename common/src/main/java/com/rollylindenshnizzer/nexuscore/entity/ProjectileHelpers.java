package com.rollylindenshnizzer.nexuscore.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ProjectileHelpers {
    public static void homeTowards(Entity projectile, Entity target, double strength, double maxSpeed) {
        Vec3 desired = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(projectile.position()).normalize().scale(strength);
        Vec3 velocity = projectile.getDeltaMovement().add(desired);
        if (velocity.length() > maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }
        projectile.setDeltaMovement(velocity);
    }

    public static void gravity(Entity projectile, boolean enabled) {
        projectile.setNoGravity(!enabled);
    }

    private ProjectileHelpers() {
    }
}
