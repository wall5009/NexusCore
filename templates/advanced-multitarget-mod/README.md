# Advanced Multi-Target Mod Template

Use target-specific folders only when common code is not enough. The included common entrypoint shows how to call optional version or loader hooks without exposing raw loader classes from your public API.

```text
common/src/main/java
mc_1_20_1/src/main/java
mc_1_21_1/src/main/java
mc_26_1_2/src/main/java
fabric/src/main/java
forge/src/main/java
neoforge/src/main/java
quilt/src/main/java
fabric_1_20_1/src/main/java
forge_1_20_1/src/main/java
quilt_1_20_1/src/main/java
fabric_1_21_1/src/main/java
neoforge_1_21_1/src/main/java
quilt_1_21_1/src/main/java
fabric_26_1_2/src/main/java
neoforge_26_1_2/src/main/java
```

The NexusCore Gradle plugin creates source sets with common, version, loader, and target directories wired into each target source set.

Run:

```bash
gradle buildAllTargets
gradle checkAllTargets
```
