package com.fosder.pocketdimension.integration.jei;

import com.fosder.pocketdimension.setup.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PocketJeiRecipeCategory implements IRecipeCategory<PocketJeiRecipe> {
    public static final ResourceLocation UID =
            new ResourceLocation("pocketdimension", "dimensional_table");

    private static final int OUTPUT_SLOT = 9;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public PocketJeiRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(160, 80);
        icon = guiHelper.createDrawableIngredient(new ItemStack(ModItems.DIMENSIONAL_TABLE_ITEM.get()));
        slot = guiHelper.getSlotDrawable();
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends PocketJeiRecipe> getRecipeClass() {
        return PocketJeiRecipe.class;
    }

    @Override
    public String getTitle() {
        return new TranslationTextComponent("gui.jei.category.pocketdimension.dimensional_table").getString();
    }

    @Override
    public ITextComponent getTitleAsTextComponent() {
        return new TranslationTextComponent("gui.jei.category.pocketdimension.dimensional_table");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(PocketJeiRecipe recipe, IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (ItemStack stack : recipe.getGrid()) {
            inputs.add(stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack));
        }
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PocketJeiRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        for (int i = 0; i < 9; i++) {
            int x = (i % 3) * 20;
            int y = (i / 3) * 20;
            stacks.init(i, true, x, y);
            ItemStack stack = recipe.getGrid().get(i);
            if (!stack.isEmpty()) {
                stacks.set(i, stack);
            }
            stacks.setBackground(i, getSlotDrawable());
        }

        stacks.init(OUTPUT_SLOT, false, 120, 30);
        stacks.set(OUTPUT_SLOT, recipe.getOutput());
        stacks.setBackground(OUTPUT_SLOT, getSlotDrawable());
    }

    private IDrawable getSlotDrawable() {
        return slot;
    }
}
