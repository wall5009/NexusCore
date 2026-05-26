package com.rollylindenshnizzer.nexuscore.neoforge.compat.rei;

import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerAdvancedControl;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerBridge;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerCategory;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerDisplayPage;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerProgressDirection;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerProgressWidget;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerRole;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerSlotWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@REIPluginClient
public final class NexusCoreNeoForgeReiPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        for (RecipeViewerCategory category : RecipeViewerBridge.categories()) {
            CategoryIdentifier<NexusReiDisplay> id = CategoryIdentifier.of(category.id());
            registry.add(new NexusReiCategory(id, category));
            for (ItemStack workstation : category.workstations()) {
                if (!workstation.isEmpty()) {
                    registry.addWorkstations(id, EntryStacks.of(workstation));
                }
            }
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (RecipeViewerCategory category : RecipeViewerBridge.categories()) {
            CategoryIdentifier<NexusReiDisplay> id = CategoryIdentifier.of(category.id());
            for (RecipeViewerDisplayPage page : RecipeViewerBridge.displayPagesFor(category)) {
                registry.add(new NexusReiDisplay(id, page));
            }
        }
    }

    private record NexusReiCategory(CategoryIdentifier<NexusReiDisplay> id, RecipeViewerCategory category)
            implements DisplayCategory<NexusReiDisplay> {
        @Override
        public CategoryIdentifier<? extends NexusReiDisplay> getCategoryIdentifier() {
            return id;
        }

        @Override
        public Component getTitle() {
            return category.title();
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(category.icon());
        }

        @Override
        public List<Widget> setupDisplay(NexusReiDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            for (var widget : display.page.page().widgets()) {
                if (widget instanceof RecipeViewerSlotWidget slot) {
                    widgets.add(slotWidget(slot, bounds));
                } else if (widget instanceof RecipeViewerProgressWidget progress) {
                    widgets.add(progressWidget(progress, bounds));
                } else if (widget instanceof com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerTextWidget text) {
                    widgets.add(Widgets.createLabel(new Point(bounds.x + text.x(), bounds.y + text.y()), text.text())
                            .color(text.color())
                            .shadow(text.shadow()));
                } else if (widget instanceof com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerTooltipWidget tooltip) {
                    widgets.add(Widgets.createTooltip(new Rectangle(bounds.x + tooltip.x(), bounds.y + tooltip.y(),
                            tooltip.width(), tooltip.height()), tooltip.lines()));
                }
            }
            for (RecipeViewerAdvancedControl control : display.page.layout().controlsFor(display.page.page())) {
                if (control.appliesTo("rei")) {
                    addControl(widgets, bounds, control);
                }
            }
            return widgets;
        }

        @Override
        public int getDisplayHeight() {
            return category.height();
        }

        @Override
        public int getDisplayWidth(NexusReiDisplay display) {
            return display.page.layout().width();
        }

        private static Widget slotWidget(RecipeViewerSlotWidget slot, Rectangle bounds) {
            var reiSlot = Widgets.createSlot(new Rectangle(bounds.x + slot.x(), bounds.y + slot.y(),
                    slot.width(), slot.height())).entries(entryIngredient(slot));
            reiSlot.backgroundEnabled(slot.drawBackground());
            return switch (slot.role()) {
                case INPUT, CATALYST -> reiSlot.markInput();
                case OUTPUT -> reiSlot.markOutput();
                case RENDER_ONLY -> reiSlot.unmarkInputOrOutput();
            };
        }

        private static Widget progressWidget(RecipeViewerProgressWidget progress, Rectangle bounds) {
            if (progress.texture() == null) {
                return Widgets.createArrow(new Point(bounds.x + progress.x(), bounds.y + progress.y()))
                        .animationDurationMS(progress.durationMillis());
            }
            return Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) ->
                    drawProgress(graphics, progress, bounds));
        }

        private static void addControl(List<Widget> widgets, Rectangle bounds, RecipeViewerAdvancedControl control) {
            switch (control.type()) {
                case "tooltip" -> widgets.add(Widgets.createTooltip(new Rectangle(
                                bounds.x + control.intProperty("x", 0),
                                bounds.y + control.intProperty("y", 0),
                                control.intProperty("width", 1),
                                control.intProperty("height", 1)),
                        Component.literal(control.property("text", ""))));
                case "button" -> widgets.add(Widgets.createButton(new Rectangle(
                                bounds.x + control.intProperty("x", 0),
                                bounds.y + control.intProperty("y", 0),
                                control.intProperty("width", 40),
                                control.intProperty("height", 20)),
                        Component.literal(control.property("text", ""))).enabled(control.booleanProperty("active", true)));
                default -> {
                }
            }
        }

        private static EntryIngredient entryIngredient(RecipeViewerSlotWidget slot) {
            List<EntryStack<?>> stacks = new ArrayList<>();
            for (ItemStack item : slot.items()) {
                stacks.add(applyTooltip(EntryStacks.of(item), slot));
            }
            for (var fluid : slot.fluids()) {
                stacks.add(applyTooltip(EntryStacks.of(fluid.fluid(), fluid.amount()), slot));
            }
            return stacks.isEmpty() ? EntryIngredient.empty() : EntryIngredient.of(stacks);
        }

        private static <T> EntryStack<T> applyTooltip(EntryStack<T> stack, RecipeViewerSlotWidget slot) {
            return slot.tooltip().isEmpty() ? stack : stack.tooltip(slot.tooltip());
        }

        private static void drawProgress(GuiGraphics graphics, RecipeViewerProgressWidget progress, Rectangle bounds) {
            double amount = progressAmount(progress);
            int drawWidth = progress.direction().horizontal()
                    ? Math.max(1, (int) Math.round(progress.width() * amount))
                    : progress.width();
            int drawHeight = progress.direction().horizontal()
                    ? progress.height()
                    : Math.max(1, (int) Math.round(progress.height() * amount));
            int x = progress.direction() == RecipeViewerProgressDirection.RIGHT_TO_LEFT
                    ? bounds.x + progress.x() + progress.width() - drawWidth
                    : bounds.x + progress.x();
            int y = progress.direction() == RecipeViewerProgressDirection.BOTTOM_TO_TOP
                    ? bounds.y + progress.y() + progress.height() - drawHeight
                    : bounds.y + progress.y();
            int u = progress.direction() == RecipeViewerProgressDirection.RIGHT_TO_LEFT
                    ? progress.u() + progress.width() - drawWidth
                    : progress.u();
            int v = progress.direction() == RecipeViewerProgressDirection.BOTTOM_TO_TOP
                    ? progress.v() + progress.height() - drawHeight
                    : progress.v();
            graphics.blit(progress.texture(), x, y, u, v, drawWidth, drawHeight,
                    progress.textureWidth(), progress.textureHeight());
        }

        private static double progressAmount(RecipeViewerProgressWidget progress) {
            if (progress.durationMillis() <= 0) {
                return progress.fullToEmpty() ? 0 : 1;
            }
            double amount = (System.currentTimeMillis() % progress.durationMillis()) / (double) progress.durationMillis();
            if (progress.fullToEmpty()) {
                amount = 1 - amount;
            }
            return Math.max(0.0, Math.min(1.0, amount));
        }
    }

    private static final class NexusReiDisplay extends BasicDisplay {
        private final CategoryIdentifier<NexusReiDisplay> category;
        private final RecipeViewerDisplayPage page;

        private NexusReiDisplay(CategoryIdentifier<NexusReiDisplay> category, RecipeViewerDisplayPage page) {
            super(ingredients(page, RecipeViewerRole.INPUT), ingredients(page, RecipeViewerRole.OUTPUT), Optional.of(page.id()));
            this.category = category;
            this.page = page;
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return category;
        }

        private static List<EntryIngredient> ingredients(RecipeViewerDisplayPage page, RecipeViewerRole role) {
            return page.page().slots().stream()
                    .filter(slot -> slot.role() == role)
                    .map(NexusReiCategory::entryIngredient)
                    .toList();
        }
    }
}
