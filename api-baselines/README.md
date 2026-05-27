# NexusCore ABI Baselines

Place released `nexuscore-common` jars here when cutting compatibility-checked releases.

The default lookup is:

```text
api-baselines/nexuscore-common-<nexusApiBaselineVersion>.jar
```

You can override it explicitly:

```bash
./gradlew checkBinaryCompatibility -PnexusApiBaseline=C:/path/to/nexuscore-common-1.1.0.jar
```

Release builds should pass `-PnexusRelease=true`; that makes the build fail if no real baseline jar is configured.
