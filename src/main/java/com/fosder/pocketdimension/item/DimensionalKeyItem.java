package com.fosder.pocketdimension.item;

import com.fosder.pocketdimension.util.PocketConfig;
import com.fosder.pocketdimension.util.PocketTeleport;
import com.fosder.pocketdimension.util.PocketText;
import com.fosder.pocketdimension.world.PocketDimension;
import com.fosder.pocketdimension.world.PocketPortalEngine;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class DimensionalKeyItem extends Item {
    public DimensionalKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) return new ActionResult<>(ActionResultType.SUCCESS, stack);
        if (!(player instanceof ServerPlayerEntity)) return new ActionResult<>(ActionResultType.PASS, stack);

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        CompoundNBT tag = stack.getOrCreateTag();

        if (!tag.hasUUID("Owner")) {
            tag.putUUID("Owner", player.getUUID());
            PocketText.send(player, "message.pocketdimension.key_bound");
        }

        UUID owner = tag.getUUID("Owner");

        if (player.isShiftKeyDown()) {
            PocketPortalEngine.openPortalNearPlayer(serverPlayer, owner, tag);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (PocketDimension.isPocketWorld(world)) {
            if (PocketTeleport.returnBack(serverPlayer, tag)) {
                PocketText.send(player, "message.pocketdimension.key_returned");
            }
        } else {
            if (PocketTeleport.enterPocket(serverPlayer, owner, tag)) {
                PocketText.send(player, "message.pocketdimension.key_entered");
            }
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.hasUUID("Owner")) {
            tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.key_owner", tag.getUUID("Owner")));
        } else {
            tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.key_unowned"));
        }
        tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.key_use"));
        tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.key_rift"));
        tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.key_direct_cost", PocketConfig.DIRECT_TELEPORT_ENERGY_COST));
        tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.key_rift_cost", PocketConfig.OPEN_RIFT_ENERGY_COST, PocketConfig.RIFT_TRAVEL_ENERGY_COST));
    }
}
