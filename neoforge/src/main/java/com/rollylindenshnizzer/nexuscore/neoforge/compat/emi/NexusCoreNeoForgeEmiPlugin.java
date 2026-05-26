package com.rollylindenshnizzer.nexuscore.neoforge.compat.emi;

import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerAdvancedControl;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerBridge;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerCategory;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerDisplayPage;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerProgressWidget;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerRole;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerSlotWidget;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@EmiEntrypoint
public final class NexusCoreNeoForgeEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        for (RecipeViewerCategory category : RecipeViewerBridge.categories()) {
            EmiRecipeCategory emiCategory = new EmiRecipeCategory(category.id(), EmiStack.of(category.icon()));
            registry.addCategory(emiCategory);
            for (ItemStack workstation : category.workstations()) {
                if (!workstation.isEmpty()) {
                    registry.addWorkstation(emiCategory, EmiStack.of(workstation));
                }
            }
            for (RecipeViewerDisplayPage page : RecipeViewerBridge.displayPagesFor(category)) {
                registry.addRecipe(new NexusEmiRecipe(emiCategory, page));
            }
        }
    }

    private static final class NexusEmiRecipe extends BasicEmiRecipe {
        private final RecipeViewerDisplayPage page;

        private NexusEmiRecipe(EmiRecipeCategory category, RecipeViewerDisplayPage page) {
            super(category, page.id(), page.layout().width(), page.layout().height());
            this.page = page;
            inputs = ingredients(page, RecipeViewerRole.INPUT);
            catalysts = ingredients(page, RecipeViewerRole.CATALYST);
            outputs = outputStacks(page);
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            for (var widget : page.page().widgets()) {
                if (widget instanceof RecipeViewerSlotWidget slot) {
                    addSlot(widgets, slot);
                } else if (widget instanceof RecipeViewerProgressWidget progress) {
                    addProgress(widgets, progress);
                } else if (widget instanceof com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerTextWidget text) {
                    widgets.addText(text.text(), text.x(), text.y(), text.color(), text.shadow());
                } else if (widget instanceof com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerTooltipWidget tooltip) {
                    widgets.addTooltipText(tooltip.lines(), tooltip.x(), tooltip.y(), tooltip.width(), tooltip.height());
                }
            }
            for (RecipeViewerAdvancedControl control : page.layout().controlsFor(page.page())) {
                if (control.appliesTo("emi")) {
                    addControl(widgets, control);
                }
            }
        }

        @Override
        public boolean supportsRecipeTree() {
            return page.layout().controlsFor(page.page()).stream()
                    .filter(control -> control.appliesTo("emi") && "recipe_tree".equals(control.type()))
                    .findFirst()
                    .map(control -> control.booleanProperty("enabled", true))
                    .orElse(true);
        }

        @Override
        public boolean hideCraftable() {
            return page.layout().controlsFor(page.page()).stream()
                    .anyMatch(control -> control.appliesTo("emi") && "hide_craftable".equals(control.type())
                            && control.booleanProperty("enabled", true));
        }

        private static List<EmiIngredient> ingredients(RecipeViewerDisplayPage page, RecipeViewerRole role) {
            return page.page().slots().stream()
                    .filter(slot -> slot.role() == role)
                    .map(NexusEmiRecipe::ingredient)
                    .filter(ingredient -> !ingredient.isEmpty())
                    .toList();
        }

        private static List<EmiStack> outputStacks(RecipeViewerDisplayPage page) {
            List<EmiStack> stacks = new ArrayList<>();
            for (RecipeViewerSlotWidget slot : page.page().slots()) {
                if (slot.role() != RecipeViewerRole.OUTPUT) {
                    continue;
                }
                slot.items().stream().map(EmiStack::of).forEach(stacks::add);
                slot.fluids().stream().map(fluid -> EmiStack.of(fluid.fluid(), fluid.amount())).forEach(stacks::add);
            }
            return stacks;
        }

        private static EmiIngredient ingredient(RecipeViewerSlotWidget slot) {
            List<EmiIngredient> ingredients = new ArrayList<>();
            slot.items().stream().map(EmiStack::of).forEach(ingredients::add);
            slot.fluids().stream().map(fluid -> EmiStack.of(fluid.fluid(), fluid.amount())).forEach(ingredients::add);
            if (ingredients.isEmpty()) {
                return EmiStack.EMPTY;
            }
            if (ingredients.size() == 1) {
                return ingredients.getFirst();
            }
            return EmiIngredient.of(ingredients);
        }

        private static void addSlot(WidgetHolder widgets, RecipeViewerSlotWidget slot) {
            EmiIngredient ingredient = ingredient(slot);
            if (slot.hasFluids() && !slot.hasItems()) {
                widgets.addTank(ingredient, slot.x(), slot.y(), slot.width(), slot.height(),
                        (int) Math.min(Integer.MAX_VALUE, slot.fluidCapacity()));
                return;
            }
            var slotWidget = widgets.addSlot(ingredient, slot.x(), slot.y())
                    .drawBack(slot.drawBackground())
                    .large(slot.large())
                    .catalyst(slot.role() == RecipeViewerRole.CATALYST);
            for (Component line : slot.tooltip()) {
                slotWidget.appendTooltip(line);
            }
        }

        private static void addProgress(WidgetHolder widgets, RecipeViewerProgressWidget progress) {
            if (progress.texture() == null) {
                widgets.addFillingArrow(progress.x(), progress.y(), progress.durationMillis());
                return;
            }
            widgets.addAnimatedTexture(progress.texture(), progress.x(), progress.y(), progress.width(), progress.height(),
                    progress.u(), progress.v(), progress.regionWidth(), progress.regionHeight(),
                    progress.textureWidth(), progress.textureHeight(), progress.durationMillis(),
                    progress.direction().horizontal(), progress.direction().reverse(), progress.fullToEmpty());
        }

        private static void addControl(WidgetHolder widgets, RecipeViewerAdvancedControl control) {
            switch (control.type()) {
                case "tooltip" -> widgets.addTooltipText(List.of(Component.literal(control.property("text", ""))),
                        control.intProperty("x", 0), control.intProperty("y", 0),
                        control.intProperty("width", 1), control.intProperty("height", 1));
                case "button" -> {
                    ResourceLocation texture = ResourceLocation.tryParse(control.property("texture", ""));
                    if (texture != null) {
                        widgets.addButton(control.intProperty("x", 0), control.intProperty("y", 0),
                                control.intProperty("width", 16), control.intProperty("height", 16),
                                control.intProperty("u", 0), control.intProperty("v", 0), texture,
                                () -> control.booleanProperty("active", true), (mouseX, mouseY, button) -> {
                                });
                    } else {
                        widgets.addButton(control.intProperty("x", 0), control.intProperty("y", 0),
                                control.intProperty("width", 16), control.intProperty("height", 16),
                                control.intProperty("u", 0), control.intProperty("v", 0),
                                () -> control.booleanProperty("active", true), (mouseX, mouseY, button) -> {
                                });
                    }
                }
                default -> {
                }
            }
        }
    }
}
