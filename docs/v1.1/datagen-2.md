# Datagen 2.0

v1.1 datagen adds:

- Content ownership through `nexus.content.json`.
- Incremental file hashing with `IncrementalDatagen`.
- Strict validation through `NexusDataValidator`.
- JSON, Markdown, and HTML reports through `DatagenReportWriters`.
- Recipe conflict checks through `RecipeDiagnostics`.
- Golden file helpers through `GoldenFiles`.

Run `runNexusValidation` in CI and publish `build/reports/nexus`.
