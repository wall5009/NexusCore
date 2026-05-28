package com.rollylindenshnizzer.nexuscore.ritual;

import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;

import java.util.Map;

@NexusIncubating(since = "1.3")
public final class NexusRitualEffects {
    public static NexusRituals.RitualEffect circleParticles(Object particle) {
        return visual("circle_particles", String.valueOf(particle)).parameter("shape", "ring");
    }

    public static NexusRituals.RitualEffect lightningParticles() {
        return visual("particles", "minecraft:electric_spark").parameter("pattern", "lightning");
    }

    public static NexusRituals.RitualEffect beam(Object particleOrTexture) {
        return visual("beam", String.valueOf(particleOrTexture));
    }

    public static NexusRituals.RitualEffect floatingText(String translationKey) {
        return visual("floating_text", translationKey);
    }

    public static NexusRituals.RitualEffect sound(Object soundId) {
        return new NexusRituals.RitualEffect("play_sound", String.valueOf(soundId),
                NexusRituals.EffectTiming.TICK, false, false, Map.of());
    }

    public static NexusRituals.RitualEffect cameraShake(double strength) {
        return visual("camera_shake", "local").parameter("strength", strength);
    }

    private static NexusRituals.RitualEffect visual(String action, String target) {
        return new NexusRituals.RitualEffect(action, target, NexusRituals.EffectTiming.TICK, false, false, Map.of());
    }

    private NexusRitualEffects() {
    }
}
