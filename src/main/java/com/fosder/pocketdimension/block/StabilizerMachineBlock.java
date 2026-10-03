package com.fosder.pocketdimension.block;

import com.fosder.pocketdimension.container.StabilizerMachineContainer;
import com.fosder.pocketdimension.setup.ModItems;
import com.fosder.pocketdimension.tile.StabilizerMachineTileEntity;
import com.fosder.pocketdimension.util.PocketStabilityData;
import com.fosder.pocketdimension.util.PocketText;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Máquina Estabilizadora: corazón técnico del bolsillo.
 * - Abre GUI con estado.
 * - Vincula llaves dimensionales.
 * - Consume combustible dimensional.
 * - Acepta recarga automática por tolvas mediante TileEntity.
 * - Aplica upgrades básicos de tamaño y seguridad.
 */
public class StabilizerMachineBlock extends Block {
    public StabilizerMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new StabilizerMachineTileEntity();
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof StabilizerMachineTileEntity) {
                InventoryHelper.dropContents(world, pos, (StabilizerMachineTileEntity) tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide) {
            return ActionResultType.SUCCESS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty() && held.getItem() == ModItems.DIMENSIONAL_KEY.get()) {
            linkKey(world, pos, player, held);
            return ActionResultType.SUCCESS;
        }

        int fuel = PocketStabilityData.fuelValue(held);
        if (fuel > 0) {
            int before = PocketStabilityData.getEnergy(player);
            int capacity = PocketStabilityData.getCapacity(player);
            if (before >= capacity) {
                PocketText.send(player, "message.pocketdimension.machine_full");
                return ActionResultType.SUCCESS;
            }
            int energy = PocketStabilityData.addEnergy(player, fuel);
            if (!player.isCreative()) {
                held.shrink(1);
            }
            PocketText.send(player, "message.pocketdimension.machine_energy_added", fuel, energy, capacity);
            return ActionResultType.SUCCESS;
        }

        if (!held.isEmpty() && held.getItem() == ModItems.SIZE_UPGRADE.get()) {
            PocketStabilityData.upgradeSize(player);
            if (!player.isCreative()) {
                held.shrink(1);
            }
            PocketText.send(player, "message.pocketdimension.machine_size_upgrade", PocketStabilityData.getSizeLevel(player));
            return ActionResultType.SUCCESS;
        }

        if (!held.isEmpty() && held.getItem() == ModItems.SECURITY_UPGRADE.get()) {
            PocketStabilityData.upgradeSecurity(player);
            if (!player.isCreative()) {
                held.shrink(1);
            }
            PocketText.send(player, "message.pocketdimension.machine_security_upgrade", PocketStabilityData.getSecurityLevel(player));
            return ActionResultType.SUCCESS;
        }

        if (!held.isEmpty() && held.getItem() == ModItems.ENERGY_UPGRADE.get()) {
            PocketStabilityData.upgradeEnergy(player);
            if (!player.isCreative()) {
                held.shrink(1);
            }
            PocketText.send(player, "message.pocketdimension.machine_energy_upgrade", PocketStabilityData.getEnergyLevel(player));
            PocketText.send(player, "message.pocketdimension.machine_capacity", PocketStabilityData.getCapacity(player));
            return ActionResultType.SUCCESS;
        }

        TileEntity tile = world.getBlockEntity(pos);
        final StabilizerMachineTileEntity machineInventory = tile instanceof StabilizerMachineTileEntity
                ? (StabilizerMachineTileEntity) tile
                : null;

        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, inventory, craftingPlayer) -> new StabilizerMachineContainer(
                        windowId,
                        inventory,
                        IWorldPosCallable.create(world, pos),
                        machineInventory
                ),
                new TranslationTextComponent("container.pocketdimension.stabilizer_machine")
        ));
        return ActionResultType.SUCCESS;
    }

    private void linkKey(World world, BlockPos pos, PlayerEntity player, ItemStack key) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof StabilizerMachineTileEntity) {
            StabilizerMachineTileEntity machine = (StabilizerMachineTileEntity) tile;
            if (machine.hasOwner() && !machine.isOwner(player.getUUID())) {
                PocketText.send(player, "message.pocketdimension.machine_already_owned", machine.getOwnerDisplayName());
                return;
            }
        }

        CompoundNBT tag = key.getOrCreateTag();
        if (!tag.contains("Owner")) {
            tag.putUUID("Owner", player.getUUID());
            tag.putString("OwnerName", player.getName().getString());
        }

        tag.putBoolean("StabilizerLinked", true);
        tag.putString("StabilizerDimension", world.dimension().location().toString());
        tag.putInt("StabilizerX", pos.getX());
        tag.putInt("StabilizerY", pos.getY());
        tag.putInt("StabilizerZ", pos.getZ());
        PocketStabilityData.linkMachine(player, world, pos);

        if (tile instanceof StabilizerMachineTileEntity) {
            ((StabilizerMachineTileEntity) tile).setOwner(player);
        }

        PocketText.send(player, "message.pocketdimension.machine_key_linked");
        PocketText.send(player, "message.pocketdimension.machine_auto_fuel");
        PocketText.send(player, "message.pocketdimension.machine_cycle", PocketStabilityData.getDrainPerCycle(player));
    }
}
