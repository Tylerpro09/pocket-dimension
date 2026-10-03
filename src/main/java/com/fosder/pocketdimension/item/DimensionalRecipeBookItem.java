package com.fosder.pocketdimension.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Libro interno de recetas de la Mesa Dimensional.
 * No usa el recipe book vanilla porque las recetas dimensionales son custom y solo existen en el container del mod.
 */
public class DimensionalRecipeBookItem extends Item {
    public DimensionalRecipeBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            sendRecipeBook(player);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    private static void sendRecipeBook(PlayerEntity player) {
        msg(player, "message.pocketdimension.recipe_book.title");
        msg(player, "message.pocketdimension.recipe_book.description");
        msg(player, "message.pocketdimension.recipe_book.void_shard");
        msg(player, "message.pocketdimension.recipe_book.pocket_core");
        msg(player, "message.pocketdimension.recipe_book.dimensional_key");
        msg(player, "message.pocketdimension.recipe_book.void_stabilizer");
        msg(player, "message.pocketdimension.recipe_book.size_upgrade");
        msg(player, "message.pocketdimension.recipe_book.security_upgrade");
        msg(player, "message.pocketdimension.recipe_book.hacker_key");
        msg(player, "message.pocketdimension.recipe_book.book");
        msg(player, "message.pocketdimension.recipe_book.tip");
    }

    private static void msg(PlayerEntity player, String key, Object... args) {
        player.sendMessage(new TranslationTextComponent(key, args), player.getUUID());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.recipe_book_use"));
        tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.recipe_book_internal"));
    }
}
