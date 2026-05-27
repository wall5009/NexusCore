package com.rollylindenshnizzer.nexuscore.block;

import com.rollylindenshnizzer.nexuscore.data.RecipeJsonBuilder;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NexusBlockSets {
    public static Builder gem(String modId, String name) {
        return new Builder(modId, name).material(MapColor.COLOR_RED).generateStorageBlock().generateOreBlock();
    }

    public static Builder stone(String modId, String name) {
        return new Builder(modId, name).material(MapColor.STONE).generateStorageBlock();
    }

    public static Builder metal(String modId, String name) {
        return new Builder(modId, name).material(MapColor.METAL).generateStorageBlock().generateRawOreBlock();
    }

    public static Builder decorative(String modId, String name) {
        return new Builder(modId, name).material(MapColor.COLOR_GRAY).generateStorageBlock();
    }

    public static final class Builder {
        private final String modId;
        private final String name;
        private MapColor material = MapColor.STONE;
        private float hardness = 3.0F;
        private float resistance = 3.0F;
        private boolean storage;
        private boolean ore;
        private boolean rawOre;
        private boolean recipes;
        private boolean tags;
        private boolean translations = true;
        private DeferredSupplier<CreativeModeTab> creativeTab;

        private Builder(String modId, String name) {
            this.modId = modId;
            this.name = name;
        }

        public Builder material(MapColor material) {
            this.material = material;
            return this;
        }

        public Builder strength(float hardness, float resistance) {
            this.hardness = hardness;
            this.resistance = resistance;
            return this;
        }

        public Builder requiresPickaxe() {
            tags = true;
            return this;
        }

        public Builder creativeTab(DeferredSupplier<CreativeModeTab> creativeTab) {
            this.creativeTab = creativeTab;
            return this;
        }

        public Builder generateStorageBlock() {
            storage = true;
            return this;
        }

        public Builder generateOreBlock() {
            ore = true;
            return this;
        }

        public Builder generateRawOreBlock() {
            rawOre = true;
            return this;
        }

        public Builder generateRecipes() {
            recipes = true;
            return this;
        }

        public Builder generateTags() {
            tags = true;
            return this;
        }

        public Builder generateTranslations() {
            translations = true;
            return this;
        }

        public NexusBlockSet register() {
            Map<String, RegistrySupplier<? extends Block>> blocks = new LinkedHashMap<>();
            if (storage) {
                blocks.put("storage", registerBlock(name + "_block"));
                if (recipes) {
                    com.rollylindenshnizzer.nexuscore.data.NexusData.plan(modId)
                            .data("recipe/" + name + "_block.json", RecipeJsonBuilder.shaped("building",
                                    modId + ":" + name + "_block", 1).pattern("XXX", "XXX", "XXX").key('X', modId + ":" + name).build());
                }
            }
            if (ore) {
                blocks.put("ore", registerBlock(name + "_ore"));
            }
            if (rawOre) {
                blocks.put("raw_ore", registerBlock("raw_" + name + "_block"));
            }
            if (translations) {
                com.rollylindenshnizzer.nexuscore.data.NexusData.plan(modId)
                        .translation("block." + modId + "." + name + "_block", human(name) + " Block");
            }
            return new NexusBlockSet(name, Map.copyOf(blocks));
        }

        private RegistrySupplier<Block> registerBlock(String path) {
            BlockBuilder<Block> builder = NexusBlocks.block(modId, path)
                    .strength(hardness, resistance)
                    .mapColor(material)
                    .withBlockItem()
                    .simpleCubeModel()
                    .dropsSelf();
            if (creativeTab != null) {
                builder.creativeTab(creativeTab);
            }
            if (tags) {
                builder.mineableWithPickaxe().requiresCorrectTool();
            }
            return builder.register();
        }

        private static String human(String value) {
            return value.replace('_', ' ');
        }
    }

    private NexusBlockSets() {
    }
}
