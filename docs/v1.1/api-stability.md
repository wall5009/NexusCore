# API Stability

NexusCore public APIs are marked with:

- `@NexusStable`: supported across minor releases.
- `@NexusExperimental`: usable, but may change with migration notes.
- `@NexusInternal`: implementation detail.
- `@NexusDeprecated`: includes `since`, `replaceWith`, and optional removal target.

Policy:

- Stable APIs get a migration path before removal.
- v1.1 supports Minecraft 1.21.1, Java 21, Architectury 13.0.8, and owo-lib 0.12.15.x.
- Release builds should run `generateApiSurfaceReport` and `checkStableApiCompatibility`.
