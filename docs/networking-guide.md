# Networking Guide

Create a channel:

```java
NexusNetwork network = NexusNetwork.create("examplemod", "main");
```

Register packets:

```java
network.serverbound("open_menu", OpenMenuPacket.class, OpenMenuPacket::decode)
    .encoder(OpenMenuPacket::encode)
    .handler((packet, context) -> context.player().openMenu(packet.menuId()))
    .register();
```

Packet handlers receive a NexusCore packet context instead of raw loader networking objects.

Packets may also implement `NexusPacket` and provide an `encode(NexusPacketBuffer)` method. If no explicit encoder is supplied, NexusCore calls that method when the packet implements `NexusPacket`.

`NexusPacketBuffer` stores simple values in unit tests and wraps native Minecraft byte buffers when a loader supplies one. Fabric-family targets that still expose the ID plus byte-buffer networking API can therefore use the same common encoder and decoder in native send/receive paths.

Forge, NeoForge, and newer custom-payload-only networking APIs need target-specific channel/payload glue for fully native packet delivery. NexusCore still registers packets in the common runtime service on those targets so tests and common setup remain deterministic.
