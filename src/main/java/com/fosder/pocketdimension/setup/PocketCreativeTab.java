package com.fosder.pocketdimension.setup;

import com.fosder.pocketdimension.PocketDimensionMod;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class PocketCreativeTab {
    public static final ItemGroup TAB = new ItemGroup(PocketDimensionMod.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.DIMENSIONAL_TABLE_ITEM.get());
        }
    };

    private PocketCreativeTab() {}
}
