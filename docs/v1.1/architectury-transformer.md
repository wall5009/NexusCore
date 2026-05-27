# Architectury Transformer Note

Some `runGametest` executions print a `StringIndexOutOfBoundsException` from `dev.architectury.transformer.TransformerRuntime.removeDuplicates` after Minecraft has already shut down and Gradle has already reported `BUILD SUCCESSFUL`.

This is treated as a non-fatal Architectury runtime cleanup issue when all of the following are true:

- The Gradle task exits with code `0`.
- The GameTest summary says all required tests passed.
- The stack trace appears after the game test server shutdown messages.

Release verification should still keep the stack trace visible in CI logs. If the Gradle task fails, or if the exception appears before the GameTest summary, treat it as a release blocker.

Current mitigation:

- The validation checklist requires both Fabric and NeoForge GameTests to pass.
- Release notes must mention the post-success transformer trace until the upstream transformer version stops printing it.
- `-PnexusRelease=true` should only be used after checking the latest Architectury Loom/runtime version in a clean CI environment.
