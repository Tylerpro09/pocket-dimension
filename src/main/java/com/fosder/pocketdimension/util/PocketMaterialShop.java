package com.fosder.pocketdimension.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Catálogo pequeño y seguro de materiales que se pueden comprar en el editor.
 * Los materiales se entregan como objetos normales: no se activa el creativo.
 */
public final class PocketMaterialShop {
    public static final int STONE = 0;
    public static final int DIRT = 1;
    public static final int GRASS = 2;
    public static final int COBBLESTONE = 3;
    public static final int GLASS = 4;
    public static final int OAK_LOG = 5;
    public static final int OAK_LEAVES = 6;
    public static final int GLOWSTONE = 7;
    public static final int WATER_BUCKET = 8;

    public static final int[] ENTRY_IDS = {
            STONE, DIRT, GRASS, COBBLESTONE, GLASS,
            OAK_LOG, OAK_LEAVES, GLOWSTONE, WATER_BUCKET
    };

    private PocketMaterialShop() {}

    public static boolean isValid(int entryId) {
        return entryId >= STONE && entryId <= WATER_BUCKET;
    }

    public static ItemStack createStack(int entryId) {
        switch (entryId) {
            case STONE: return new ItemStack(Items.STONE, 16);
            case DIRT: return new ItemStack(Items.DIRT, 16);
            case GRASS: return new ItemStack(Items.GRASS_BLOCK, 16);
            case COBBLESTONE: return new ItemStack(Items.COBBLESTONE, 16);
            case GLASS: return new ItemStack(Items.GLASS, 16);
            case OAK_LOG: return new ItemStack(Items.OAK_LOG, 8);
            case OAK_LEAVES: return new ItemStack(Items.OAK_LEAVES, 16);
            case GLOWSTONE: return new ItemStack(Items.GLOWSTONE, 8);
            case WATER_BUCKET: return new ItemStack(Items.WATER_BUCKET);
            default: return ItemStack.EMPTY;
        }
    }

    public static int cost(int entryId) {
        switch (entryId) {
            case STONE: return 20;
            case DIRT: return 12;
            case GRASS: return 24;
            case COBBLESTONE: return 16;
            case GLASS: return 24;
            case OAK_LOG: return 20;
            case OAK_LEAVES: return 12;
            case GLOWSTONE: return 32;
            case WATER_BUCKET: return 40;
            default: return Integer.MAX_VALUE;
        }
    }

    public static String labelKey(int entryId) {
        switch (entryId) {
            case STONE: return "screen.pocketdimension.shop.stone";
            case DIRT: return "screen.pocketdimension.shop.dirt";
            case GRASS: return "screen.pocketdimension.shop.grass";
            case COBBLESTONE: return "screen.pocketdimension.shop.cobblestone";
            case GLASS: return "screen.pocketdimension.shop.glass";
            case OAK_LOG: return "screen.pocketdimension.shop.oak_log";
            case OAK_LEAVES: return "screen.pocketdimension.shop.oak_leaves";
            case GLOWSTONE: return "screen.pocketdimension.shop.glowstone";
            case WATER_BUCKET: return "screen.pocketdimension.shop.water_bucket";
            default: return "screen.pocketdimension.shop.unknown";
        }
    }
}
