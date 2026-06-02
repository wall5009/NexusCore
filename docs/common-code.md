# Common Code

Most mods should use `common/src/main/java`.

Common code should depend on NexusCore APIs:

```java
NexusItems.create(MOD, "ruby")
    .creativeTab("ingredients")
    .register();
```

Avoid direct Fabric, Forge, NeoForge, Quilt, or raw Minecraft version classes in public common code. If a feature is unavailable on a target, check:

```java
if (NexusPlatform.supports(NexusFeature.CLIENT_RENDERING)) {
    // register client rendering
}
```
