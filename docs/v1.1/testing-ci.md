# Testing and CI

Recommended CI jobs:

- `./gradlew build`
- `./gradlew runNexusValidation`
- `./gradlew :fabric:runGametest`
- `./gradlew :neoforge:runGametest`

Use `GoldenFiles` for generated JSON snapshots and `PacketTestHarness` for packet tests.
