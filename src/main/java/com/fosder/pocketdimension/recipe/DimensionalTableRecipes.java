package com.fosder.pocketdimension.recipe;

import com.fosder.pocketdimension.setup.ModItems;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Recetas privadas de la Mesa Dimensional.
 * Estas recetas NO existen en la mesa normal de Minecraft.
 */
public final class DimensionalTableRecipes {
    private DimensionalTableRecipes() {}

    public static ItemStack findResult(CraftingInventory matrix) {
        if (matrix == null || matrix.getContainerSize() < 9) {
            return ItemStack.EMPTY;
        }

        // Fragmento del Vacío x4
        // O E O
        // _ Q _
        // O E O
        if (matches(matrix,
                Items.OBSIDIAN, Items.ENDER_PEARL, Items.OBSIDIAN,
                null, Items.QUARTZ, null,
                Items.OBSIDIAN, Items.ENDER_PEARL, Items.OBSIDIAN)) {
            return new ItemStack(ModItems.VOID_SHARD.get(), 4);
        }



        // Batería Estabilizadora x2 desde mena con Silk Touch o creativo
        // _ _ _
        // _ O _
        // _ _ _
        if (matches(matrix,
                null, null, null,
                null, ModItems.STABILIZER_BATTERY_ORE_ITEM.get(), null,
                null, null, null)) {
            return new ItemStack(ModItems.STABILIZER_BATTERY.get(), 2);
        }

        // Batería Estabilizadora x1 comprimida
        // V R V
        // R E R
        // V R V
        if (matches(matrix,
                ModItems.VOID_SHARD.get(), Items.REDSTONE, ModItems.VOID_SHARD.get(),
                Items.REDSTONE, Items.ENDER_PEARL, Items.REDSTONE,
                ModItems.VOID_SHARD.get(), Items.REDSTONE, ModItems.VOID_SHARD.get())) {
            return new ItemStack(ModItems.STABILIZER_BATTERY.get(), 1);
        }

        // Núcleo de Bolsillo
        // D V D
        // V N V
        // D V D
        if (matches(matrix,
                Items.DIAMOND, ModItems.VOID_SHARD.get(), Items.DIAMOND,
                ModItems.VOID_SHARD.get(), Items.NETHER_STAR, ModItems.VOID_SHARD.get(),
                Items.DIAMOND, ModItems.VOID_SHARD.get(), Items.DIAMOND)) {
            return new ItemStack(ModItems.POCKET_CORE.get(), 1);
        }

        // Llave Dimensional
        // G E G
        // _ C _
        // _ S _
        if (matches(matrix,
                Items.GOLD_INGOT, Items.ENDER_EYE, Items.GOLD_INGOT,
                null, ModItems.POCKET_CORE.get(), null,
                null, Items.STICK, null)) {
            return new ItemStack(ModItems.DIMENSIONAL_KEY.get(), 1);
        }

        // Estabilizador del Vacío
        // O B O
        // B C B
        // O B O
        if (matches(matrix,
                Items.OBSIDIAN, Items.BLAZE_ROD, Items.OBSIDIAN,
                Items.BLAZE_ROD, ModItems.POCKET_CORE.get(), Items.BLAZE_ROD,
                Items.OBSIDIAN, Items.BLAZE_ROD, Items.OBSIDIAN)) {
            return new ItemStack(ModItems.VOID_STABILIZER.get(), 1);
        }


        // Máquina Estabilizadora
        // O S O
        // R C R
        // O L O
        if (matches(matrix,
                Items.OBSIDIAN, ModItems.VOID_STABILIZER.get(), Items.OBSIDIAN,
                Items.REDSTONE_BLOCK, ModItems.POCKET_CORE.get(), Items.REDSTONE_BLOCK,
                Items.OBSIDIAN, Items.SEA_LANTERN, Items.OBSIDIAN)) {
            return new ItemStack(ModItems.STABILIZER_MACHINE_ITEM.get(), 1);
        }

        // Mejora de Tamaño
        // E D E
        // D C D
        // E D E
        if (matches(matrix,
                Items.ENDER_EYE, Items.DIAMOND, Items.ENDER_EYE,
                Items.DIAMOND, ModItems.POCKET_CORE.get(), Items.DIAMOND,
                Items.ENDER_EYE, Items.DIAMOND, Items.ENDER_EYE)) {
            return new ItemStack(ModItems.SIZE_UPGRADE.get(), 1);
        }

        // Mejora de Seguridad
        // O N O
        // N C N
        // O R O
        if (matches(matrix,
                Items.OBSIDIAN, Items.NETHERITE_INGOT, Items.OBSIDIAN,
                Items.NETHERITE_INGOT, ModItems.POCKET_CORE.get(), Items.NETHERITE_INGOT,
                Items.OBSIDIAN, Items.REDSTONE_BLOCK, Items.OBSIDIAN)) {
            return new ItemStack(ModItems.SECURITY_UPGRADE.get(), 1);
        }

        // Llave Hacker
        // N S N
        // S D S
        // N B N
        if (matches(matrix,
                Items.NETHERITE_INGOT, ModItems.SECURITY_UPGRADE.get(), Items.NETHERITE_INGOT,
                ModItems.SECURITY_UPGRADE.get(), ModItems.DIMENSIONAL_KEY.get(), ModItems.SECURITY_UPGRADE.get(),
                Items.NETHERITE_INGOT, Items.BEACON, Items.NETHERITE_INGOT)) {
            return new ItemStack(ModItems.HACKER_KEY.get(), 1);
        }


        // Portal Dimensional x6 
        // V E V
        // E C E
        // V E V
        if (matches(matrix,
                ModItems.VOID_SHARD.get(), Items.ENDER_EYE, ModItems.VOID_SHARD.get(),
                Items.ENDER_EYE, ModItems.POCKET_CORE.get(), Items.ENDER_EYE,
                ModItems.VOID_SHARD.get(), Items.ENDER_EYE, ModItems.VOID_SHARD.get())) {
            return new ItemStack(ModItems.DIMENSIONAL_PORTAL_ITEM.get(), 6);
        }

        return ItemStack.EMPTY;
    }

    public static void consumeIngredients(CraftingInventory matrix) {
        for (int i = 0; i < matrix.getContainerSize(); i++) {
            ItemStack stack = matrix.getItem(i);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.getCount() <= 0) {
                    matrix.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private static boolean matches(CraftingInventory matrix, Item... pattern) {
        if (pattern.length != 9) {
            return false;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = matrix.getItem(i);
            Item expected = pattern[i];

            if (expected == null) {
                if (!stack.isEmpty()) {
                    return false;
                }
            } else {
                if (stack.isEmpty() || stack.getItem() != expected) {
                    return false;
                }
            }
        }
        return true;
    }
}
