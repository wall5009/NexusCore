# Config Schema and Sync

Use `ConfigSchemaExporter.jsonSchema(config, version)` and `ConfigSchemaExporter.markdown(config, version)` for docs and CI.

Options now carry:

- Group
- Translation key
- Enabled/visible state
- Dependencies
- Conflicts
- Restart/world reload/server sync metadata

Use `ConfigDependencyGraph.analyze(config)` and `ConfigSyncDiagnostics.compare(client, server, version)` for diagnostics.
