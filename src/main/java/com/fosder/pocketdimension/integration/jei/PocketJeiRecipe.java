package com.fosder.pocketdimension.integration.jei;

import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Datos de una receta privada de la Mesa Dimensional para JEI. */
public final class PocketJeiRecipe {
    private final List<ItemStack> grid;
    private final ItemStack output;

    public PocketJeiRecipe(ItemStack output, ItemStack... grid) {
        if (grid.length != 9) {
            throw new IllegalArgumentException("A dimensional recipe must contain exactly 9 slots");
        }
        this.grid = Arrays.asList(grid);
        this.output = output;
    }

    public List<ItemStack> getGrid() {
        return Collections.unmodifiableList(grid);
    }

    public ItemStack getOutput() {
        return output;
    }
}
