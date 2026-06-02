package com.rollylindenshnizzer.nexuscore.testmod;

import com.rollylindenshnizzer.nexuscore.api.datagen.NexusDataGen;

public final class TestDataGen {
    private TestDataGen() {
    }

    public static void register() {
        NexusDataGen.create("nexuscore_testmod")
            .recipe("test_block", """
                {
                  "type": "minecraft:crafting_shaped",
                  "pattern": ["GGG", "GGG", "GGG"],
                  "key": {
                    "G": { "item": "nexuscore_testmod:test_gem" }
                  },
                  "result": { "item": "nexuscore_testmod:test_block" }
                }
                """)
            .lootTable("blocks/test_block", """
                {
                  "type": "minecraft:block",
                  "pools": [{
                    "rolls": 1,
                    "entries": [{ "type": "minecraft:item", "name": "nexuscore_testmod:test_block" }]
                  }]
                }
                """)
            .tag("items", "test_gems", """
                {
                  "replace": false,
                  "values": ["nexuscore_testmod:test_gem"]
                }
                """)
            .blockstate("test_block", """
                {
                  "variants": {
                    "": { "model": "nexuscore_testmod:block/test_block" }
                  }
                }
                """)
            .model("block/test_block", """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": { "all": "nexuscore_testmod:block/test_block" }
                }
                """)
            .model("item/test_gem", """
                {
                  "parent": "minecraft:item/generated",
                  "textures": { "layer0": "nexuscore_testmod:item/test_gem" }
                }
                """)
            .lang("en_us", """
                {
                  "item.nexuscore_testmod.test_gem": "Test Gem",
                  "block.nexuscore_testmod.test_block": "Test Block",
                  "itemGroup.nexuscore_testmod.test_tab": "NexusCore Test Mod"
                }
                """)
            .build()
            .run();
    }
}
