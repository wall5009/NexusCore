package com.rollylindenshnizzer.nexuscore.fabric.worldgen;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.worldgen.BiomeSelector;
import com.rollylindenshnizzer.nexuscore.worldgen.NexusWorldgen;
import com.rollylindenshnizzer.nexuscore.worldgen.OreGenerationBuilder;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.function.Predicate;

public final class FabricWorldgenRegistrar {
    private static boolean installed;

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        NexusWorldgen.onOreRegistered(FabricWorldgenRegistrar::registerOre);
    }

    private static void registerOre(OreGenerationBuilder ore) {
        ResourceKey<PlacedFeature> feature = ResourceKey.create(Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(ore.modId(), ore.path()));
        BiomeModifications.addFeature(selector(ore.biomes()), GenerationStep.Decoration.UNDERGROUND_ORES, feature);
        NexusCore.LOGGER.debug("Registered Fabric biome feature {}", feature.location());
    }

    private static Predicate<BiomeSelectionContext> selector(BiomeSelector selector) {
        Predicate<BiomeSelectionContext> include = selector.includes().isEmpty()
                ? BiomeSelectors.all()
                : selector.includes().stream().map(FabricWorldgenRegistrar::singleSelector).reduce(Predicate::or).orElse(BiomeSelectors.all());
        Predicate<BiomeSelectionContext> exclude = selector.excludes().stream().map(FabricWorldgenRegistrar::singleSelector).reduce(Predicate::or).orElse(context -> false);
        return include.and(exclude.negate());
    }

    private static Predicate<BiomeSelectionContext> singleSelector(String value) {
        if (value == null || value.isBlank() || value.equals("*")) {
            return BiomeSelectors.all();
        }
        if (value.equals("#minecraft:is_overworld")) {
            return BiomeSelectors.foundInOverworld();
        }
        if (value.equals("#minecraft:is_nether")) {
            return BiomeSelectors.foundInTheNether();
        }
        if (value.equals("#minecraft:is_end")) {
            return BiomeSelectors.foundInTheEnd();
        }
        if (value.startsWith("#")) {
            ResourceLocation id = ResourceLocation.tryParse(value.substring(1));
            if (id == null) {
                return context -> false;
            }
            return BiomeSelectors.tag(TagKey.create(Registries.BIOME, id));
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return context -> false;
        }
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, id);
        return BiomeSelectors.includeByKey(key);
    }

    private FabricWorldgenRegistrar() {
    }
}
