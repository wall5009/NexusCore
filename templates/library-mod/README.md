# Library Mod Template

Use this layout when publishing common APIs for other mods.

```text
library-mod/
  settings.gradle.kts
  build.gradle.kts
  common/src/main/java/
  fabric_1_21_1/
  neoforge_1_21_1/
```

Keep public API types free of raw loader classes. Route loader-specific behavior through NexusCore services or your own internal bridge package.

Run:

```bash
gradle buildAllTargets publish
```
