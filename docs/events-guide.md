# Events Guide

Register common, server, client, player, and tick events in initialization code:

```java
NexusEvents.SERVER_STARTED.register(server -> {
    System.out.println("Server started");
});

NexusEvents.PLAYER_JOINED.register(player -> {
    player.sendMessage("Welcome");
});
```

Client-only work should use `CLIENT_STARTED` and capability checks.
