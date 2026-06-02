# NexusCore v2

NexusCore v2 is a beginner-friendly Minecraft modding API that exposes stable common-code APIs, target-aware service providers, target validation, generated loader metadata, native Minecraft object bridges, and a full test-mod matrix for Fabric, Forge, NeoForge, and Quilt targets.

This repository is organized as a multi-project Gradle build. The public Java packages are rooted at:

```java
com.rollylindenshnizzer.nexuscore
```

Run local verification with:

```bash
gradle build checkAllTargets validateNexusPackages
```

Build release artifacts with:

```bash
gradle publishAllTargets
```

The release bundle is written to `build/distributions/nexuscore-2.0.0.zip`, and Maven artifacts are written to `build/repo`.
