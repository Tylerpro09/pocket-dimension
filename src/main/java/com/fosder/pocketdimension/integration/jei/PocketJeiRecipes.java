package com.fosder.pocketdimension.integration.jei;

import com.fosder.pocketdimension.setup.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Lista visible en JEI de las recetas privadas de la Mesa Dimensional. */
final class PocketJeiRecipes {
    private PocketJeiRecipes() {}

    static List<PocketJeiRecipe> create() {
        List<PocketJeiRecipe> recipes = new ArrayList<>();

        recipes.add(recipe(stack(ModItems.VOID_SHARD.get(), 4),
                stack(Items.OBSIDIAN), stack(Items.ENDER_PEARL), stack(Items.OBSIDIAN),
                empty(), stack(Items.QUARTZ), empty(),
                stack(Items.OBSIDIAN), stack(Items.ENDER_PEARL), stack(Items.OBSIDIAN)));

        recipes.add(recipe(stack(ModItems.STABILIZER_BATTERY.get(), 2),
                empty(), empty(), empty(),
                empty(), stack(ModItems.STABILIZER_BATTERY_ORE_ITEM.get()), empty(),
                empty(), empty(), empty()));

        recipes.add(recipe(stack(ModItems.STABILIZER_BATTERY.get()),
                stack(ModItems.VOID_SHARD.get()), stack(Items.REDSTONE), stack(ModItems.VOID_SHARD.get()),
                stack(Items.REDSTONE), stack(Items.ENDER_PEARL), stack(Items.REDSTONE),
                stack(ModItems.VOID_SHARD.get()), stack(Items.REDSTONE), stack(ModItems.VOID_SHARD.get())));

        recipes.add(recipe(stack(ModItems.POCKET_CORE.get()),
                stack(Items.DIAMOND), stack(ModItems.VOID_SHARD.get()), stack(Items.DIAMOND),
                stack(ModItems.VOID_SHARD.get()), stack(Items.NETHER_STAR), stack(ModItems.VOID_SHARD.get()),
                stack(Items.DIAMOND), stack(ModItems.VOID_SHARD.get()), stack(Items.DIAMOND)));

        recipes.add(recipe(stack(ModItems.DIMENSIONAL_KEY.get()),
                stack(Items.GOLD_INGOT), stack(Items.ENDER_EYE), stack(Items.GOLD_INGOT),
                empty(), stack(ModItems.POCKET_CORE.get()), empty(),
                empty(), stack(Items.STICK), empty()));

        recipes.add(recipe(stack(ModItems.VOID_STABILIZER.get()),
                stack(Items.OBSIDIAN), stack(Items.BLAZE_ROD), stack(Items.OBSIDIAN),
                stack(Items.BLAZE_ROD), stack(ModItems.POCKET_CORE.get()), stack(Items.BLAZE_ROD),
                stack(Items.OBSIDIAN), stack(Items.BLAZE_ROD), stack(Items.OBSIDIAN)));

        recipes.add(recipe(stack(ModItems.STABILIZER_MACHINE_ITEM.get()),
                stack(Items.OBSIDIAN), stack(ModItems.VOID_STABILIZER.get()), stack(Items.OBSIDIAN),
                stack(Items.REDSTONE_BLOCK), stack(ModItems.POCKET_CORE.get()), stack(Items.REDSTONE_BLOCK),
                stack(Items.OBSIDIAN), stack(Items.SEA_LANTERN), stack(Items.OBSIDIAN)));

        recipes.add(recipe(stack(ModItems.SIZE_UPGRADE.get()),
                stack(Items.ENDER_EYE), stack(Items.DIAMOND), stack(Items.ENDER_EYE),
                stack(Items.DIAMOND), stack(ModItems.POCKET_CORE.get()), stack(Items.DIAMOND),
                stack(Items.ENDER_EYE), stack(Items.DIAMOND), stack(Items.ENDER_EYE)));

        recipes.add(recipe(stack(ModItems.SECURITY_UPGRADE.get()),
                stack(Items.OBSIDIAN), stack(Items.NETHERITE_INGOT), stack(Items.OBSIDIAN),
                stack(Items.NETHERITE_INGOT), stack(ModItems.POCKET_CORE.get()), stack(Items.NETHERITE_INGOT),
                stack(Items.OBSIDIAN), stack(Items.REDSTONE_BLOCK), stack(Items.OBSIDIAN)));

        recipes.add(recipe(stack(ModItems.HACKER_KEY.get()),
                stack(Items.NETHERITE_INGOT), stack(ModItems.SECURITY_UPGRADE.get()), stack(Items.NETHERITE_INGOT),
                stack(ModItems.SECURITY_UPGRADE.get()), stack(ModItems.DIMENSIONAL_KEY.get()), stack(ModItems.SECURITY_UPGRADE.get()),
                stack(Items.NETHERITE_INGOT), stack(Items.BEACON), stack(Items.NETHERITE_INGOT)));

        recipes.add(recipe(stack(ModItems.DIMENSIONAL_PORTAL_ITEM.get(), 6),
                stack(ModItems.VOID_SHARD.get()), stack(Items.ENDER_EYE), stack(ModItems.VOID_SHARD.get()),
                stack(Items.ENDER_EYE), stack(ModItems.POCKET_CORE.get()), stack(Items.ENDER_EYE),
                stack(ModItems.VOID_SHARD.get()), stack(Items.ENDER_EYE), stack(ModItems.VOID_SHARD.get())));

        return Collections.unmodifiableList(recipes);
    }

    private static PocketJeiRecipe recipe(ItemStack output, ItemStack... grid) {
        return new PocketJeiRecipe(output, grid);
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }

    private static ItemStack stack(Item item, int count) {
        return new ItemStack(item, count);
    }

    private static ItemStack empty() {
        return ItemStack.EMPTY;
    }
}
