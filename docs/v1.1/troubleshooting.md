# Troubleshooting Common Errors

| Code | Cause | Fix |
| --- | --- | --- |
| `NC-CRASH-CLIENT-ON-SERVER` | Client class loaded on a dedicated server | Move screen/render code behind client entrypoints |
| `NC-CRASH-REGISTRY-EARLY` | Registry supplier accessed too early | Access after registry registration |
| `NC-DOC-DATAGEN` | Generated data issue | Run datagen and follow report suggestions |
| `NC-DOC-CONFIG` | Config dependency graph issue | Fix missing or circular dependencies |
| `NC-DOC-NETWORK` | Packet or protocol mismatch | Align protocol versions and packet IDs |
