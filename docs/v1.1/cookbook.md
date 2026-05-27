# v1.1 Cookbook

- Basic item: `NexusItems.item(modid, "ruby").modelGenerated().register()`
- Basic block: `NexusBlocks.block(modid, "ruby_block").withBlockItem().simpleCubeModel().dropsSelf().register()`
- Block set: `NexusBlockSets.gem(modid, "ruby").generateRecipes().register()`
- Synced config: `intOption("cost", 100).serverSynced()`
- Packet protocol: `NexusNetworking.channel(modid, "main").protocolVersion("1.1")`
- Data component: `NexusComponents.item(modid, "mode").codec(codec).networkSynced().register()`
- GameTest: use registered loader GameTest suites plus `NexusAssertions`.
- Recipe viewer: use `RecipeViewerDisplay.builder(...)`.
