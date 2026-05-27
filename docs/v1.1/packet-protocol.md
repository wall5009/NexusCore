# Packet Protocol

Use:

```java
NexusNetworking.channel(modid, "main")
    .protocolVersion("1.1")
    .disconnectOnMismatch((client, server) -> "Client uses " + client + ", server uses " + server);
```

Diagnostics record protocol version and packet IDs. Use `PacketTestHarness` for encode/decode round-trip tests and `NetworkMonitor` for packet count/size summaries.
