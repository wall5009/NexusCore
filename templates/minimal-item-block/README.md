# Minimal Item/Block Mod Template

Use the NexusCore Gradle plugin:

```gradle
plugins {
    id "com.rollylindenshnizzer.nexuscore" version "1.1.0-build.1"
}
```

Run:

```text
./gradlew nexusSetupProject nexusCreateItem -PnexusName=ruby
./gradlew nexusCreateBlock -PnexusName=ruby_block
```

The generated stubs include source files, datagen hooks, language key placeholders, and TODO markers.
