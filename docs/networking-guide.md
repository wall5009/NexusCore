# Networking Guide

`NexusNetworking` wraps Architectury networking into named, versioned channels and pairs it with diagnostics, packet guards, request/response helpers, sync batching, and test harnesses.

## Channels

```java
NexusNetworking.ChannelBuilder channel = NexusNetworking.channel(MOD_ID, "main")
        .protocolVersion("1.2")
        .disconnectOnMismatch((client, server) ->
                "Protocol mismatch: client=" + client + ", server=" + server);
```

Use one channel per logical protocol. Bump the version when packet payloads or required packet sets are not backward compatible.

## Packet Registration

The channel can register serverbound and clientbound Architectury simple messages:

```java
channel.serverbound("set_mode", SetModePacket::new);
channel.clientbound("sync_state", SyncStatePacket::new);
```

The registered packet paths are tracked in `NexusNetworking.diagnostics()`.

## Handshake Diagnostics

Each channel stores:

- Channel ID.
- Local protocol version.
- Registered packet names.
- Mismatch message factory.

Compare remote metadata:

```java
NexusNetworking.ChannelDiagnostics diagnostics = NexusNetworking.diagnostics().get(channel.id("main"));
HandshakeReport report = diagnostics.compare(remoteVersion, remotePackets);
```

Use this in debug screens and support reports.

## Packet Guards

Packet handlers should validate before mutating anything:

```java
PacketGuards.requireServerSide(packetId, context);
ServerPlayer player = PacketGuards.requireServerPlayer(packetId, context);
PacketGuards.runOnMainThread(context, () -> mutateState(player));
```

Pair packet guards with `ServerAuthority`:

- `requireServer`
- `antiSpam`
- `withinDistance`
- `sameDimension`
- `canOpenMenu`

## Request/Response

`RequestResponse<R>` tracks pending async-style flows:

```java
RequestResponse<String> requests = new RequestResponse<>();
UUID id = requests.begin(Duration.ofSeconds(5));
requests.success(id, "accepted");
Optional<RequestResponse.Result<String>> result = requests.poll(id);
```

## Sync Batching

`SyncBatcher<T>` groups dirty values and flushes them after a throttle interval:

```java
SyncBatcher<ResourceLocation> batcher = new SyncBatcher<>(Duration.ofMillis(50));
batcher.markDirty(id("ruby_press"));
batcher.flushIfReady(values -> sendSync(values));
```

Use `SyncProfile` to document sync intent: always, nearby players, menu viewers, chunk watchers, or manual.

## Network Monitor

```java
NetworkMonitor.record(packetId, byteCount, exceptionOrNull);
Map<ResourceLocation, NetworkMonitor.Stats> snapshot = NetworkMonitor.snapshot();
```

Stats include count, total bytes, exception count, and average bytes.

## Packet Test Harness

Use this for pure-Java payload tests:

```java
PacketTestHarness.assertRoundTrip(new ExamplePacket(42),
        (buffer, packet) -> buffer.writeInt(packet.value()),
        buffer -> new ExamplePacket(buffer.readInt()));
```

This catches serialization mismatches without starting Minecraft.

## Menus

Register menus through `NexusMenus`:

```java
RegistrySupplier<MenuType<ChestMenu>> menu = NexusMenus.menu(MOD_ID, "tutorial_chest_menu", ChestMenu::threeRows);
```

Use `MenuBinding` and `MenuDebugInfo` to document target bindings, quick-move routes, and sync fields.

## Example

The scaffolding and GameTest coverage declares a main channel in `scaffolded content` and a diagnostics channel in `NexusCoreGameTestScenarios`. It also records network stats, tests a packet round trip, demonstrates request/response, and flushes a sync batch.
