package com.fosder.pocketdimension.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;

/**
 * Variante del libro que abre la guía Patchouli cuando esa dependencia está instalada.
 * Si Patchouli no está disponible, ModItems registra el libro normal como respaldo.
 */
public final class PatchouliRecipeBookItem extends DimensionalRecipeBookItem {
    private static final ResourceLocation BOOK_ID =
            new ResourceLocation("pocketdimension", "dimensional_guide");

    public PatchouliRecipeBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide && player instanceof ServerPlayerEntity) {
            PatchouliAPI.IPatchouliAPI api = PatchouliAPI.get();
            if (!api.isStub()) {
                api.openBookGUI((ServerPlayerEntity) player, BOOK_ID);
            } else {
                return super.use(world, player, hand);
            }
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
