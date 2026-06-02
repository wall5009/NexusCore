# Datagen Guide

NexusCore datagen writes common resources through the active target bridge:

```java
NexusDataGen.create("examplemod")
    .recipe("ruby_block", "{ ... }")
    .blockstate("ruby_block", "{ ... }")
    .model("block/ruby_block", "{ ... }")
    .lang("en_us", "{ ... }")
    .build()
    .run();
```

The Gradle plugin creates `run...Datagen` tasks for every supported target.
