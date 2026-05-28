# Scaffolding Walkthrough

NexusCore no longer ships an embedded example mod. Use the Gradle plugin tasks, cookbook snippets, and GameTest scenarios as the maintained examples.

Run scaffolds with a lowercase path:

```powershell
.\gradlew.bat nexusCreateMachine -PnexusName=alloy_smelter -PnexusModId=yourmod
.\gradlew.bat nexusCreateDimension -PnexusName=moon -PnexusModId=yourmod
.\gradlew.bat nexusCreateStructure -PnexusName=ancient_tower -PnexusModId=yourmod
.\gradlew.bat nexusCreateAutomationNetwork -PnexusName=starter_network -PnexusModId=yourmod
```

Use `-PnexusDryRun=true` to preview generated source. Use `-PnexusOverwrite=true` only when you intentionally want to replace a generated file.

The generated classes are starter points, not hidden runtime magic. They create NexusCore descriptors and validation hooks that you still wire into your own mod lifecycle, datagen entrypoint, and loader registration.

