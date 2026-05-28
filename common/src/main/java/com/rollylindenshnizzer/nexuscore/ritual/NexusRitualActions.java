package com.rollylindenshnizzer.nexuscore.ritual;

import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

@NexusIncubating(since = "1.3")
public final class NexusRitualActions {
    public static NexusRituals.RitualEffect spawnEntity(Object entityId) {
        return server("spawn_entity", idValue(entityId)).parameter("count", 1);
    }

    public static NexusRituals.RitualEffect spawnItem(Object itemId, int count) {
        return server("spawn_item", idValue(itemId)).parameter("count", count);
    }

    public static NexusRituals.RitualEffect changeBlock(Object blockId) {
        return server("change_block", idValue(blockId));
    }

    public static NexusRituals.RitualEffect transformArea(Object blockId, int radius) {
        return server("transform_area", idValue(blockId)).parameter("radius", radius).asDangerous();
    }

    public static NexusRituals.RitualEffect setWeather(NexusWeather weather) {
        return server("set_weather", weather.serializedName());
    }

    public static NexusRituals.RitualEffect clearWeather() {
        return setWeather(NexusWeather.CLEAR);
    }

    public static NexusRituals.RitualEffect teleportPlayer(Object destination) {
        return server("teleport_player", idValue(destination));
    }

    public static NexusRituals.RitualEffect openPortal(Object dimensionId) {
        return server("open_portal", idValue(dimensionId)).asDangerous();
    }

    public static NexusRituals.RitualEffect applyStatusEffect(Object effectId, int durationTicks, int amplifier) {
        return server("apply_status_effect", idValue(effectId))
                .parameter("duration_ticks", durationTicks)
                .parameter("amplifier", amplifier);
    }

    public static NexusRituals.RitualEffect grantAdvancement(Object advancementId) {
        return server("grant_advancement", idValue(advancementId));
    }

    public static NexusRituals.RitualEffect consumeIngredients() {
        return server("consume_ingredients", "all");
    }

    public static NexusRituals.RitualEffect drainEnergy(long amount) {
        return server("drain_energy", "nexuscore:energy").parameter("amount", amount);
    }

    public static NexusRituals.RitualEffect drainFluid(Object fluidId, long amount) {
        return server("drain_fluid", idValue(fluidId)).parameter("amount", amount);
    }

    public static NexusRituals.RitualEffect sendMessage(String translationKey) {
        return server("send_message", translationKey);
    }

    private static NexusRituals.RitualEffect server(String action, String target) {
        return new NexusRituals.RitualEffect(action, target, NexusRituals.EffectTiming.COMPLETE, true, false, Map.of());
    }

    private static String idValue(Object value) {
        if (value instanceof ResourceLocation id) {
            return id.toString();
        }
        return String.valueOf(value);
    }

    private NexusRitualActions() {
    }
}
