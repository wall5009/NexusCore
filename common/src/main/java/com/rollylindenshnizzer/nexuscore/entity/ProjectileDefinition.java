package com.rollylindenshnizzer.nexuscore.entity;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

@NexusStable(since = "1.2")
public record ProjectileDefinition(double damage,
                                   float speed,
                                   float divergence,
                                   float gravity,
                                   int pierceLevel,
                                   boolean pickupAllowed) {
    public ProjectileDefinition {
        damage = Math.max(0.0, damage);
        speed = Math.max(0.0F, speed);
        divergence = Math.max(0.0F, divergence);
        gravity = Math.max(0.0F, gravity);
        pierceLevel = Math.max(0, pierceLevel);
    }

    public static ProjectileDefinition simple(double damage, float speed) {
        return new ProjectileDefinition(damage, speed, 1.0F, 0.05F, 0, false);
    }
}
