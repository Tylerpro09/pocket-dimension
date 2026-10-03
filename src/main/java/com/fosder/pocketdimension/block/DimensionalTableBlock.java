package com.fosder.pocketdimension.block;

import com.fosder.pocketdimension.container.DimensionalWorkbenchContainer;
import com.fosder.pocketdimension.setup.ModItems;
import com.fosder.pocketdimension.util.PocketText;
import com.fosder.pocketdimension.world.PocketDimension;
import com.fosder.pocketdimension.world.PocketWorldBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Mesa Dimensional.
 *
 * Click derecho normal: abre el GUI propio del mod.
 * Shift + click derecho: funciones rápidas de administración del bolsillo.
 */
public class DimensionalTableBlock extends Block {
    public DimensionalTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide) {
            return ActionResultType.SUCCESS;
        }

        if (!player.isShiftKeyDown()) {
            player.openMenu(new SimpleNamedContainerProvider(
                    (windowId, inventory, craftingPlayer) -> new DimensionalWorkbenchContainer(
                            windowId,
                            inventory,
                            IWorldPosCallable.create(world, pos)
                    ),
                    new TranslationTextComponent("container.pocketdimension.dimensional_table")
            ));
            return ActionResultType.SUCCESS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.isEmpty()) {
            ItemStack key = new ItemStack(ModItems.DIMENSIONAL_KEY.get());
            key.getOrCreateTag().putUUID("Owner", player.getUUID());
            key.getOrCreateTag().putString("OwnerName", player.getName().getString());
            player.addItem(key);
            PocketText.send(player, "message.pocketdimension.table_key_created");
            PocketText.send(player, "message.pocketdimension.table_open_gui");
            return ActionResultType.SUCCESS;
        }

        if (held.getItem() == ModItems.VOID_STABILIZER.get()) {
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                ServerWorld pocket = sp.getServer().getLevel(PocketDimension.POCKET_WORLD);
                if (pocket != null) {
                    PocketWorldBuilder.rebuildPocket(pocket, player.getUUID());
                    PocketText.send(player, "message.pocketdimension.table_rebuilt");
                    if (!player.isCreative()) {
                        held.shrink(1);
                    }
                } else {
                    PocketText.send(player, "message.pocketdimension.world_missing");
                }
            }
            return ActionResultType.SUCCESS;
        }

        if (held.getItem() == ModItems.SIZE_UPGRADE.get()) {
            PocketText.send(player, "message.pocketdimension.table_size_upgrade");
            PocketText.send(player, "message.pocketdimension.table_size_config");
            return ActionResultType.SUCCESS;
        }

        if (held.getItem() == ModItems.SECURITY_UPGRADE.get()) {
            PocketText.send(player, "message.pocketdimension.table_security_upgrade");
            return ActionResultType.SUCCESS;
        }

        PocketText.send(player, "message.pocketdimension.table_help_title");
        PocketText.send(player, "message.pocketdimension.table_help_normal");
        PocketText.send(player, "message.pocketdimension.table_help_key");
        PocketText.send(player, "message.pocketdimension.table_help_stabilizer");
        return ActionResultType.SUCCESS;
    }
}
