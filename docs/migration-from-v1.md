# Migration From v1

Move old common helpers into `common/src/main/java` and replace direct loader calls with NexusCore v2 APIs.

Typical replacements:

| v1 style | v2 style |
|---|---|
| raw registry calls | `NexusItems`, `NexusBlocks`, `NexusRegistries` |
| loader event bus calls | `NexusEvents` |
| loader command callbacks | `NexusCommands` |
| custom config bootstrap | `NexusConfig` |
| duplicated metadata files | shared `nexuscore {}` metadata generation |

Target-specific code can move into loader, version, or loader-version folders as needed.
