# Recipe Viewer Advanced Controls

NexusCore now treats advanced recipe-viewer controls as a portable control model with an explicit support matrix.

Use the factory helpers on `RecipeViewerAdvancedControl`:

```java
page.control(RecipeViewerAdvancedControl.button("all", 6, 48, 40, 16, "Info"));
page.control(RecipeViewerAdvancedControl.recipeTransferButton(92, 48));
page.control(RecipeViewerAdvancedControl.recipeTree(true));
page.control(RecipeViewerAdvancedControl.hideCraftable(false));
page.control(RecipeViewerAdvancedControl.badge("all", 4, 4, "Heated"));
```

Native support:

| Control | JEI | EMI | REI |
| --- | --- | --- | --- |
| tooltip | yes | yes | yes |
| button | fallback badge/tooltip | yes | yes |
| recipe transfer button | yes | fallback tooltip | fallback tooltip |
| shapeless marker | yes | fallback tooltip | fallback tooltip |
| recipe tree | fallback tooltip | yes | fallback tooltip |
| hide craftable | fallback tooltip | yes | fallback tooltip |
| badge | yes | yes | yes |

Set `strict=true` on a control to fail fast when a viewer cannot support it natively. Without strict mode, unsupported controls render deterministic fallbacks instead of silently disappearing.
