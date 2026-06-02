# Config Guide

Create simple configs:

```java
NexusConfig config = NexusConfig.create("examplemod")
    .booleanValue("enableRuby", true)
    .intValue("rubySpawnRate", 8, 0, 64)
    .build();
```

The active service provider resolves the target config path and writes JSON defaults when a file does not exist.
