package com.fosder.pocketdimension.block;

import com.fosder.pocketdimension.util.PocketConfig;
import com.fosder.pocketdimension.util.PocketTeleport;
import com.fosder.pocketdimension.util.PocketText;
import com.fosder.pocketdimension.world.PocketDimension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Grieta dimensional física temporal.
 * No reemplaza portales vanilla: solo funciona en la dimensión bolsillo y mundos normales.
 */
public class DimensionalPortalBlock extends Block {
    public DimensionalPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide) {
            return ActionResultType.SUCCESS;
        }
        if (!(player instanceof ServerPlayerEntity)) {
            return ActionResultType.PASS;
        }
        return activatePortal((ServerPlayerEntity) player, world)
                ? ActionResultType.SUCCESS
                : ActionResultType.FAIL;
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClientSide || !(entity instanceof ServerPlayerEntity)) {
            return;
        }

        activatePortal((ServerPlayerEntity) entity, world);
    }

    private static boolean activatePortal(ServerPlayerEntity player, World world) {
        long now = world.getGameTime();
        CompoundNBT persistent = player.getPersistentData();
        long cooldownUntil = persistent.getLong("PocketPortalCooldownUntil");
        if (cooldownUntil > now) {
            return false;
        }
        persistent.putLong("PocketPortalCooldownUntil", now + 60L);

        CompoundNBT storage = getPortalStorage(player);

        if (PocketDimension.isPocketWorld(world)) {
            if (PocketTeleport.returnBackFromRift(player, storage)) {
                PocketText.send(player, "message.pocketdimension.rift_returned");
                return true;
            }
        } else {
            if (PocketTeleport.enterPocketFromRift(player, player.getUUID(), storage)) {
                PocketText.send(player, "message.pocketdimension.rift_entered");
                return true;
            }
        }
        persistent.putLong("PocketPortalCooldownUntil", now);
        return false;
    }

    private static CompoundNBT getPortalStorage(ServerPlayerEntity player) {
        CompoundNBT persistent = player.getPersistentData();
        CompoundNBT root = persistent.getCompound(PocketConfig.TAG_ROOT);
        persistent.put(PocketConfig.TAG_ROOT, root);
        return root;
    }
}
