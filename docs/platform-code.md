# Platform Code

Use loader folders for loader-specific implementation:

```text
fabric/src/main/java
forge/src/main/java
neoforge/src/main/java
quilt/src/main/java
```

Platform code should stay behind internal bridges. Public mod APIs should keep using NexusCore wrappers so most of the mod remains shared.
