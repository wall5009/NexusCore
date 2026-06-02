# Registry Guide

Items:

```java
NexusItems.create(MOD, "ruby")
    .creativeTab("ingredients")
    .register();
```

Blocks with block items:

```java
NexusBlocks.create(MOD, "ruby_block")
    .strength(4.0f)
    .requiresTool()
    .withSimpleItem()
    .register();
```

Creative tabs:

```java
NexusCreativeTabs.create(MOD, "main")
    .title("Example Mod")
    .icon("examplemod:ruby")
    .entry("examplemod:ruby")
    .register();
```
