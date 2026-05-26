# Networking Guide

`NexusNetworking` wraps Architectury networking into named, versioned channels.

## Create A Channel

```java
NexusNetworking.channel(MOD_ID, "main")
        .version("1");
```

Use one channel per logical protocol. Bump the version when packet payloads are not backward compatible.

## Packet Guards

`PacketGuards` centralizes checks before handling packets:

- Side and environment checks.
- Permission checks.
- Null or invalid payload checks.
- Friendly `PacketValidationException` errors.

Keep packet validation close to the handler boundary. Common game logic should receive already-validated inputs.

## Practical Pattern

1. Declare the channel during `onInitialize()`.
2. Register packet types from common code where possible.
3. Gate client-only handlers behind loader/client entrypoints.
4. Use `PacketGuards` before mutating world, player, or block entity state.
