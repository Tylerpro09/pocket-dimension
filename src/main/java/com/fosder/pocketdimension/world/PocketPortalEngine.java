package com.fosder.pocketdimension.world;

import com.fosder.pocketdimension.setup.ModBlocks;
import com.fosder.pocketdimension.util.PocketConfig;
import com.fosder.pocketdimension.util.PocketStabilityData;
import com.fosder.pocketdimension.util.PocketText;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Motor de grietas dimensionales del mod.
 *
 * La llave abre una grieta temporal 2x3. El motor borra automaticamente
 * los bloques pocketdimension:dimensional_portal cuando vence su duracion.
 */
public final class PocketPortalEngine {
    private PocketPortalEngine() {}

    public static boolean openPortalNearPlayer(ServerPlayerEntity player, UUID owner, CompoundNBT keyTag) {
        return openTemporaryRiftNearPlayer(player, owner, keyTag);
    }

    public static boolean openTemporaryRiftNearPlayer(ServerPlayerEntity player, UUID owner, CompoundNBT keyTag) {
        World world = player.level;
        if (world == null || world.isClientSide) return false;

        Direction facing = player.getDirection();
        Direction right = facing.getClockWise();
        BlockPos base = findSafeBase(world, player.blockPosition().relative(facing, 2));
        BlockPos origin = base.relative(right, -1);

        if (!canReplaceRiftArea(world, origin, right)) {
            PocketText.send(player, "message.pocketdimension.rift.no_space");
            return false;
        }

        if (!player.isCreative() && !PocketStabilityData.tryConsumeEnergy(player, PocketConfig.OPEN_RIFT_ENERGY_COST)) {
            PocketText.send(player, "message.pocketdimension.rift.energy_missing");
            return false;
        }

        BlockState rift = ModBlocks.DIMENSIONAL_PORTAL.get().defaultBlockState();
        List<BlockPos> placedBlocks = new ArrayList<>();

        for (int x = 0; x < PocketConfig.getRiftWidth(); x++) {
            for (int y = 0; y < PocketConfig.getRiftHeight(); y++) {
                BlockPos p = origin.relative(right, x).above(y);
                world.setBlock(p, rift, 3);
                placedBlocks.add(p.immutable());
            }
        }

        long expiresAt = world.getGameTime() + PocketConfig.getRiftDurationTicks();
        getSavedData((ServerWorld) world).addRift(placedBlocks, expiresAt);

        if (keyTag != null) {
            keyTag.putBoolean(PocketConfig.TAG_RIFT_OPENED, true);
            keyTag.putLong(PocketConfig.TAG_RIFT_EXPIRES, expiresAt);
            keyTag.putString("PocketRiftDim", world.dimension().location().toString());
            keyTag.putInt("PocketRiftX", origin.getX());
            keyTag.putInt("PocketRiftY", origin.getY());
            keyTag.putInt("PocketRiftZ", origin.getZ());
        }

        int seconds = PocketConfig.getRiftDurationTicks() / 20;
        PocketText.send(player, "message.pocketdimension.rift.opened");
        PocketText.send(player, "message.pocketdimension.rift.expires", seconds);
        return true;
    }

    public static void tickTemporaryRifts(ServerWorld world) {
        if (world == null) return;

        PocketRiftSavedData savedData = getSavedData(world);
        long now = world.getGameTime();

        List<PocketRiftSavedData.Rift> expired = new ArrayList<>();
        for (PocketRiftSavedData.Rift rift : savedData.getRifts()) {
            if (now < rift.getExpiresAt()) continue;

            closeRift(world, savedData, rift);
            expired.add(rift);
        }
        for (PocketRiftSavedData.Rift rift : expired) {
            savedData.remove(rift);
        }
    }

    private static void closeRift(ServerWorld world, PocketRiftSavedData savedData, PocketRiftSavedData.Rift rift) {
        for (long packed : rift.getBlocks()) {
            BlockPos pos = BlockPos.of(packed);
            if (!savedData.hasOtherRiftAt(pos, rift)
                    && world.getBlockState(pos).getBlock() == ModBlocks.DIMENSIONAL_PORTAL.get()) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static PocketRiftSavedData getSavedData(ServerWorld world) {
        return world.getDataStorage().computeIfAbsent(PocketRiftSavedData::new, PocketRiftSavedData.ID);
    }

    private static BlockPos findSafeBase(World world, BlockPos start) {
        BlockPos pos = start;
        while (pos.getY() > 4 && world.isEmptyBlock(pos.below())) {
            pos = pos.below();
        }
        return pos.above();
    }

    private static boolean canReplaceRiftArea(World world, BlockPos origin, Direction right) {
        for (int x = 0; x < PocketConfig.getRiftWidth(); x++) {
            for (int y = 0; y < PocketConfig.getRiftHeight(); y++) {
                BlockPos p = origin.relative(right, x).above(y);
                if (!world.isEmptyBlock(p)) {
                    return false;
                }
            }
        }
        return true;
    }

}
