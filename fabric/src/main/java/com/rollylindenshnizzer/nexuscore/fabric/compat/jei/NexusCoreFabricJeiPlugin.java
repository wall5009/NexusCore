package com.rollylindenshnizzer.nexuscore.fabric.compat.jei;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerAdvancedControl;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerBridge;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerCategory;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerDisplayPage;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerProgressDirection;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerProgressWidget;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerRole;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerSlotWidget;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerTooltipWidget;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JeiPlugin
public final class NexusCoreFabricJeiPlugin implements IModPlugin {
    private final Map<ResourceLocation, RecipeType<RecipeViewerDisplayPage>> types = new LinkedHashMap<>();

    @Override
    public ResourceLocation getPluginUid() {
        return NexusCore.id("fabric_jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        for (RecipeViewerCategory category : RecipeViewerBridge.categories()) {
            RecipeType<RecipeViewerDisplayPage> type = typeFor(category);
            registration.addRecipeCategories(new NexusJeiCategory(type, category, gui));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (RecipeViewerCategory category : RecipeViewerBridge.categories()) {
            List<RecipeViewerDisplayPage> pages = RecipeViewerBridge.displayPagesFor(category);
            if (!pages.isEmpty()) {
                registration.addRecipes(typeFor(category), pages);
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (RecipeViewerCategory category : RecipeViewerBridge.categories()) {
            RecipeType<RecipeViewerDisplayPage> type = typeFor(category);
            for (ItemStack workstation : category.workstations()) {
                if (!workstation.isEmpty()) {
                    registration.addRecipeCatalyst(workstation, type);
                }
            }
        }
    }

    private RecipeType<RecipeViewerDisplayPage> typeFor(RecipeViewerCategory category) {
        return types.computeIfAbsent(category.id(), id ->
                RecipeType.create(id.getNamespace(), id.getPath(), RecipeViewerDisplayPage.class));
    }

    private record NexusJeiCategory(RecipeType<RecipeViewerDisplayPage> type, RecipeViewerCategory category,
                                    IDrawable icon)
            implements IRecipeCategory<RecipeViewerDisplayPage> {
        private NexusJeiCategory(RecipeType<RecipeViewerDisplayPage> type, RecipeViewerCategory category,
                                 IGuiHelper gui) {
            this(type, category, gui.createDrawableItemStack(category.icon()));
        }

        @Override
        public RecipeType<RecipeViewerDisplayPage> getRecipeType() {
            return type;
        }

        @Override
        public Component getTitle() {
            return category.title();
        }

        @Override
        public int getWidth() {
            return category.width();
        }

        @Override
        public int getHeight() {
            return category.height();
        }

        @Override
        public IDrawable getIcon() {
            return icon;
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, RecipeViewerDisplayPage display, IFocusGroup focuses) {
            for (RecipeViewerSlotWidget slot : display.page().slots()) {
                var slotBuilder = builder.addSlot(role(slot.role()), slot.x(), slot.y());
                if (slot.drawBackground()) {
                    if (slot.role() == RecipeViewerRole.OUTPUT) {
                        slotBuilder.setOutputSlotBackground();
                    } else {
                        slotBuilder.setStandardSlotBackground();
                    }
                }
                if (!slot.name().isEmpty()) {
                    slotBuilder.setSlotName(slot.name());
                }
                if (!slot.tooltip().isEmpty()) {
                    slotBuilder.addRichTooltipCallback((view, tooltip) -> tooltip.addAll(slot.tooltip()));
                }
                for (ItemStack stack : slot.items()) {
                    slotBuilder.addItemStack(stack);
                }
                if (slot.hasFluids()) {
                    slotBuilder.setFluidRenderer(slot.fluidCapacity(), true, slot.width(), slot.height());
                    for (var fluid : slot.fluids()) {
                        slotBuilder.addFluidStack(fluid.fluid(), fluid.amount());
                    }
                }
            }
            for (RecipeViewerAdvancedControl control : display.layout().controlsFor(display.page())) {
                if (control.appliesTo("jei")) {
                    applyControl(builder, control);
                }
            }
        }

        @Override
        public void draw(RecipeViewerDisplayPage recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                         double mouseX, double mouseY) {
            for (RecipeViewerProgressWidget progress : recipe.page().progressWidgets()) {
                drawProgress(graphics, progress);
            }
            for (var text : recipe.page().textWidgets()) {
                graphics.drawString(Minecraft.getInstance().font, text.text(), text.x(), text.y(), text.color(), text.shadow());
            }
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip, RecipeViewerDisplayPage recipe,
                               IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
            for (RecipeViewerTooltipWidget widget : recipe.page().tooltipWidgets()) {
                if (widget.contains(mouseX, mouseY)) {
                    tooltip.addAll(widget.lines());
                }
            }
            for (RecipeViewerAdvancedControl control : recipe.layout().controlsFor(recipe.page())) {
                if (control.appliesTo("jei") && "tooltip".equals(control.type())
                        && contains(control, mouseX, mouseY)) {
                    tooltip.add(Component.literal(control.property("text", "")));
                }
            }
        }

        private static void applyControl(IRecipeLayoutBuilder builder, RecipeViewerAdvancedControl control) {
            switch (control.type()) {
                case "recipe_transfer_button" ->
                        builder.moveRecipeTransferButton(control.intProperty("x", 0), control.intProperty("y", 0));
                case "shapeless" -> {
                    if (control.properties().containsKey("x") && control.properties().containsKey("y")) {
                        builder.setShapeless(control.intProperty("x", 0), control.intProperty("y", 0));
                    } else {
                        builder.setShapeless();
                    }
                }
                default -> {
                }
            }
        }

        private static RecipeIngredientRole role(RecipeViewerRole role) {
            return switch (role) {
                case INPUT -> RecipeIngredientRole.INPUT;
                case OUTPUT -> RecipeIngredientRole.OUTPUT;
                case CATALYST -> RecipeIngredientRole.CATALYST;
                case RENDER_ONLY -> RecipeIngredientRole.RENDER_ONLY;
            };
        }

        private static boolean contains(RecipeViewerAdvancedControl control, double mouseX, double mouseY) {
            int x = control.intProperty("x", 0);
            int y = control.intProperty("y", 0);
            int width = control.intProperty("width", 1);
            int height = control.intProperty("height", 1);
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        }

        private static void drawProgress(GuiGraphics graphics, RecipeViewerProgressWidget progress) {
            double amount = progressAmount(progress);
            if (progress.texture() == null) {
                graphics.fill(progress.x(), progress.y(), progress.x() + progress.width(), progress.y() + progress.height(), 0x66000000);
                fillProgress(graphics, progress, amount, 0xFF3F8F55);
                return;
            }

            int drawWidth = progress.direction().horizontal()
                    ? Math.max(1, (int) Math.round(progress.width() * amount))
                    : progress.width();
            int drawHeight = progress.direction().horizontal()
                    ? progress.height()
                    : Math.max(1, (int) Math.round(progress.height() * amount));
            int x = progress.direction() == RecipeViewerProgressDirection.RIGHT_TO_LEFT
                    ? progress.x() + progress.width() - drawWidth
                    : progress.x();
            int y = progress.direction() == RecipeViewerProgressDirection.BOTTOM_TO_TOP
                    ? progress.y() + progress.height() - drawHeight
                    : progress.y();
            int u = progress.direction() == RecipeViewerProgressDirection.RIGHT_TO_LEFT
                    ? progress.u() + progress.width() - drawWidth
                    : progress.u();
            int v = progress.direction() == RecipeViewerProgressDirection.BOTTOM_TO_TOP
                    ? progress.v() + progress.height() - drawHeight
                    : progress.v();
            graphics.blit(progress.texture(), x, y, u, v, drawWidth, drawHeight,
                    progress.textureWidth(), progress.textureHeight());
        }

        private static void fillProgress(GuiGraphics graphics, RecipeViewerProgressWidget progress, double amount,
                                         int color) {
            int drawWidth = progress.direction().horizontal()
                    ? Math.max(1, (int) Math.round(progress.width() * amount))
                    : progress.width();
            int drawHeight = progress.direction().horizontal()
                    ? progress.height()
                    : Math.max(1, (int) Math.round(progress.height() * amount));
            int x = progress.direction() == RecipeViewerProgressDirection.RIGHT_TO_LEFT
                    ? progress.x() + progress.width() - drawWidth
                    : progress.x();
            int y = progress.direction() == RecipeViewerProgressDirection.BOTTOM_TO_TOP
                    ? progress.y() + progress.height() - drawHeight
                    : progress.y();
            graphics.fill(x, y, x + drawWidth, y + drawHeight, color);
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
}
