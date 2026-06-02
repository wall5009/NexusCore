# Client And Server Separation

Use target capabilities before registering client-only systems:

```java
if (NexusPlatform.supports(NexusFeature.CLIENT_RENDERING)) {
    NexusClient.onStarted(client -> {
        // register client renderers
    });
}
```

Server lifecycle work should use `NexusLifecycle.serverStarted(...)` or `NexusEvents.SERVER_STARTED`.
