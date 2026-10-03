package com.fosder.pocketdimension.world;

import com.fosder.pocketdimension.setup.ModBlocks;
import com.fosder.pocketdimension.tile.StabilizerMachineTileEntity;
import com.fosder.pocketdimension.util.PocketStabilityData;
import com.fosder.pocketdimension.util.PocketText;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Motor del Colapso Oscuro.
 *
 * Si la Máquina Estabilizadora del dueño queda en 0 de energía, se abre una
 * Grieta de Oscuridad Dimensional en la zona de sus estabilizadores. La grieta
 * absorbe bloques, items y mobs en círculos cada vez más grandes hasta que no
 * quede ningún estabilizador del dueño en el área cargada.
 */
public final class PocketDarkCollapseEngine {
    private PocketDarkCollapseEngine() {}

    private static final int SCAN_RADIUS = 96;
    private static final int MAX_CONSUME_RADIUS = 18;
    private static final int BLOCKS_PER_STABILIZER_PER_TICK = 42;

    public static void tickPlayer(ServerPlayerEntity player) {
        if (player == null || player.level == null || player.level.isClientSide) return;
        if (!PocketStabilityData.isMachineLinked(player)) return;

        RegistryKey<World> key = PocketStabilityData.getMachineDimensionKey(player);
        if (key == null || player.getServer() == null) return;

        ServerWorld world = player.getServer().getLevel(key);
        if (world == null) return;

        BlockPos linkedPos = PocketStabilityData.getMachinePos(player);

        // Si se recargó a tiempo, el colapso se detiene y limpia las grietas oscuras cercanas.
        if (PocketStabilityData.getEnergy(player) > 0) {
            if (PocketStabilityData.isDarkCollapseActive(player)) {
                clearDarkRifts(world, linkedPos, 32);
                PocketStabilityData.setDarkCollapseActive(player, false);
                PocketText.send(player, "message.pocketdimension.collapse.recovered");
            }
            return;
        }

        List<BlockPos> ownedMachines = findOwnedStabilizers(world, linkedPos, player.getUUID());
        if (ownedMachines.isEmpty()) {
            // Do not conclude that the machine was destroyed just because its
            // chunk is currently unloaded. The player can be in another
            // dimension while the linked machine chunk is kept unloaded.
            if (!world.getChunkSource().hasChunk(linkedPos.getX() >> 4, linkedPos.getZ() >> 4)) {
                return;
            }
            clearDarkRifts(world, linkedPos, SCAN_RADIUS);
            PocketStabilityData.setMachineLinked(player, false);
            PocketStabilityData.setDarkCollapseActive(player, false);
            PocketText.send(player, "message.pocketdimension.collapse.closed");
            return;
        }

        int stage = PocketStabilityData.advanceDarkCollapseStage(player);
        int consumeRadius = Math.min(MAX_CONSUME_RADIUS, 2 + stage / 2);
        boolean allowMachineAbsorb = stage >= 8;

        if (stage == 1 || stage % 10 == 0) {
            PocketText.send(player, "message.pocketdimension.collapse.active");
        }

        for (BlockPos machinePos : ownedMachines) {
            placeDarkRift(world, machinePos, stage);
            consumeAround(world, machinePos, consumeRadius, allowMachineAbsorb);
            absorbEntities(world, machinePos, consumeRadius + 1);
        }
    }

    private static List<BlockPos> findOwnedStabilizers(ServerWorld world, BlockPos center, UUID owner) {
        List<BlockPos> list = new ArrayList<>();
        int minX = center.getX() - SCAN_RADIUS;
        int maxX = center.getX() + SCAN_RADIUS;
        int minY = Math.max(1, center.getY() - 32);
        int maxY = Math.min(255, center.getY() + 32);
        int minZ = center.getZ() - SCAN_RADIUS;
        int maxZ = center.getZ() + SCAN_RADIUS;

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        // Inspect block-entity maps of already loaded chunks instead of
        // querying up to 2.4 million block positions every second.
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk chunk = world.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;

                for (TileEntity tile : chunk.getBlockEntities().values()) {
                    BlockPos pos = tile.getBlockPos();
                    if (pos.getX() < minX || pos.getX() > maxX
                            || pos.getY() < minY || pos.getY() > maxY
                            || pos.getZ() < minZ || pos.getZ() > maxZ) {
                        continue;
                    }
                    if (tile instanceof StabilizerMachineTileEntity
                            && ((StabilizerMachineTileEntity) tile).isOwner(owner)) {
                        list.add(pos.immutable());
                    }
                }
            }
        }
        return list;
    }

    private static void placeDarkRift(ServerWorld world, BlockPos machinePos, int stage) {
        BlockState rift = ModBlocks.DARK_DIMENSIONAL_RIFT.get().defaultBlockState();
        BlockPos core = machinePos.above(2);
        setIfReplaceable(world, core, rift);
        setIfReplaceable(world, core.north(), rift);
        setIfReplaceable(world, core.south(), rift);
        setIfReplaceable(world, core.east(), rift);
        setIfReplaceable(world, core.west(), rift);

        if (stage >= 6) {
            setIfReplaceable(world, core.above(), rift);
            setIfReplaceable(world, core.below(), rift);
        }
    }

    private static void setIfReplaceable(ServerWorld world, BlockPos pos, BlockState state) {
        Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR || block == ModBlocks.DARK_DIMENSIONAL_RIFT.get()) {
            world.setBlock(pos, state, 3);
        }
    }

    private static void consumeAround(ServerWorld world, BlockPos center, int radius, boolean allowMachineAbsorb) {
        int consumed = 0;
        int r2 = radius * radius;
        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minY = Math.max(1, center.getY() - radius);
        int maxY = Math.min(255, center.getY() + radius + 3);
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (consumed >= BLOCKS_PER_STABILIZER_PER_TICK) return;
            int dx = pos.getX() - center.getX();
            int dy = pos.getY() - center.getY();
            int dz = pos.getZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz > r2) continue;

            BlockState state = world.getBlockState(pos);
            if (!canAbsorb(state, allowMachineAbsorb)) continue;

            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            consumed++;
        }
    }

    private static boolean canAbsorb(BlockState state, boolean allowMachineAbsorb) {
        Block block = state.getBlock();
        if (state.isAir()) return false;
        if (block == Blocks.BEDROCK || block == Blocks.BARRIER || block == Blocks.COMMAND_BLOCK) return false;
        if (block == ModBlocks.DARK_DIMENSIONAL_RIFT.get()) return false;
        if (block == ModBlocks.DIMENSIONAL_PORTAL.get()) return false;
        if (block == ModBlocks.STABILIZER_MACHINE.get() && !allowMachineAbsorb) return false;
        return true;
    }

    private static void absorbEntities(ServerWorld world, BlockPos center, int radius) {
        AxisAlignedBB box = new AxisAlignedBB(center).inflate(radius);
        for (Entity entity : world.getEntitiesOfClass(Entity.class, box)) {
            if (entity instanceof ServerPlayerEntity) continue;
            if (entity instanceof ItemEntity || entity instanceof MobEntity) {
                entity.remove();
            }
        }
    }

    private static void clearDarkRifts(ServerWorld world, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -32, -radius), center.offset(radius, 32, radius))) {
            if (world.getBlockState(pos).getBlock() == ModBlocks.DARK_DIMENSIONAL_RIFT.get()) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
