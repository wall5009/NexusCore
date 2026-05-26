package com.rollylindenshnizzer.nexuscore.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.Holder;

public final class FoodBuilder {
    private final FoodProperties.Builder builder = new FoodProperties.Builder();

    public FoodBuilder nutrition(int nutrition) {
        builder.nutrition(nutrition);
        return this;
    }

    public FoodBuilder saturation(float saturation) {
        builder.saturationModifier(saturation);
        return this;
    }

    public FoodBuilder alwaysEdible() {
        builder.alwaysEdible();
        return this;
    }

    public FoodBuilder fast() {
        builder.fast();
        return this;
    }

    public FoodBuilder effect(Holder<MobEffect> effect, int durationTicks, int amplifier, float chance) {
        builder.effect(new MobEffectInstance(effect, durationTicks, amplifier), chance);
        return this;
    }

    public FoodBuilder convertsTo(ItemLike item) {
        builder.usingConvertsTo(item);
        return this;
    }

    public FoodProperties build() {
        return builder.build();
    }
}
