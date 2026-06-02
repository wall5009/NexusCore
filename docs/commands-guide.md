# Commands Guide

Commands use a small NexusCore context wrapper:

```java
NexusCommands.literal("example")
    .executes(ctx -> {
        ctx.reply("Hello from NexusCore!");
        return 1;
    })
    .register();
```

Use `permission("example.command")` or `requires(...)` when a command should be restricted.
