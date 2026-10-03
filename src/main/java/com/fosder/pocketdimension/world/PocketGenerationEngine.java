package com.fosder.pocketdimension.world;

import com.fosder.pocketdimension.util.PocketConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

import java.util.UUID;

/**
 * Genera una isla plana de césped, tierra y piedra. La dimensión base queda
 * en aire; fuera de la isla no se coloca terreno.
 */
public final class PocketGenerationEngine {
    private PocketGenerationEngine() {}

    public static BlockPos preparePocket(ServerWorld world, UUID owner) {
        BlockPos center = PocketMath.centerFor(owner);
        applyPocketWorldRules(world);

        if (world.isEmptyBlock(center) || world.isEmptyBlock(center.below())) {
            buildPocketOnly(world, center);
        } else {
            repairEssentials(world, center);
        }
        return center;
    }

    public static void rebuildPocket(ServerWorld world, UUID owner) {
        BlockPos center = PocketMath.centerFor(owner);
        applyPocketWorldRules(world);
        buildPocketOnly(world, center);
    }

    public static void applyPocketWorldRules(ServerWorld world) {
        if (world.getServer() != null) {
            world.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, world.getServer());
            world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(true, world.getServer());
            world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, world.getServer());
        }
    }

    private static void buildPocketOnly(ServerWorld world, BlockPos center) {
        int half = PocketConfig.getHalfSize();
        int clearDepth = Math.max(8, PocketConfig.getIslandThickness() + 2);
        int clearHeight = Math.max(8, PocketConfig.getBorderHeight() + 2);

        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                for (int y = -clearDepth; y <= clearHeight; y++) {
                    world.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int x = -half + 1; x <= half - 1; x++) {
            for (int z = -half + 1; z <= half - 1; z++) {
                buildFlatColumn(world, center, x, z);
            }
        }

        buildVisibleBorder(world, center, half);
        decorateFlatSurface(world, center);
        clearSpawnSpace(world, center);
    }

    private static void repairEssentials(ServerWorld world, BlockPos center) {
        if (world.isEmptyBlock(center)) {
            world.setBlock(center, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        }
        clearSpawnSpace(world, center);
    }

    private static void buildFlatColumn(ServerWorld world, BlockPos center, int x, int z) {
        if (x * x + z * z > PocketConfig.getIslandRadius() * PocketConfig.getIslandRadius()) return;

        for (int y = -PocketConfig.getIslandThickness(); y <= 0; y++) {
            BlockState state;
            if (y == 0) {
                state = Blocks.GRASS_BLOCK.defaultBlockState();
            } else if (y >= -2) {
                state = Blocks.DIRT.defaultBlockState();
            } else {
                state = Blocks.STONE.defaultBlockState();
            }
            world.setBlock(center.offset(x, y, z), state, 3);
        }
    }

    private static void clearSpawnSpace(ServerWorld world, BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                world.setBlock(center.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
                world.setBlock(center.offset(x, 0, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                world.setBlock(center.offset(x, 1, z), Blocks.AIR.defaultBlockState(), 3);
                world.setBlock(center.offset(x, 2, z), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static void buildVisibleBorder(ServerWorld world, BlockPos center, int half) {
        BlockState border = Blocks.RED_STAINED_GLASS.defaultBlockState();
        for (int y = 0; y < PocketConfig.getBorderHeight(); y++) {
            for (int offset = -half; offset <= half; offset++) {
                world.setBlock(center.offset(-half, y, offset), border, 3);
                world.setBlock(center.offset(half, y, offset), border, 3);
                world.setBlock(center.offset(offset, y, -half), border, 3);
                world.setBlock(center.offset(offset, y, half), border, 3);
            }
        }
    }

    private static void decorateFlatSurface(ServerWorld world, BlockPos center) {
        int half = PocketConfig.getHalfSize();
        int pondX = -half / 3;
        int pondZ = half / 4;

        for (int x = -half + 4; x <= half - 4; x++) {
            for (int z = -half + 4; z <= half - 4; z++) {
                BlockPos top = findSurface(world, center, x, z);
                if (top == null || !world.isEmptyBlock(top.above())) continue;

                int dx = x - pondX;
                int dz = z - pondZ;
                if (dx * dx + dz * dz <= 10) {
                    world.setBlock(top, Blocks.WATER.defaultBlockState(), 3);
                    world.setBlock(top.below(), Blocks.CLAY.defaultBlockState(), 3);
                    continue;
                }

                int hash = terrainHash(center, x, z);
                if ((hash & 31) == 0) {
                    world.setBlock(top.above(), Blocks.GRASS.defaultBlockState(), 3);
                } else if ((hash & 63) == 1) {
                    world.setBlock(top.above(), Blocks.FERN.defaultBlockState(), 3);
                } else if ((hash & 127) == 2) {
                    world.setBlock(top.above(), Blocks.DANDELION.defaultBlockState(), 3);
                } else if ((hash & 255) == 3) {
                    world.setBlock(top.above(), Blocks.OAK_SAPLING.defaultBlockState(), 3);
                }
            }
        }
    }

    private static BlockPos findSurface(ServerWorld world, BlockPos center, int x, int z) {
        for (int y = PocketConfig.getIslandThickness() + 2;
             y >= -PocketConfig.getIslandThickness() - 1;
             y--) {
            BlockPos pos = center.offset(x, y, z);
            if (!world.isEmptyBlock(pos)) {
                return pos;
            }
        }
        return null;
    }

    private static int terrainHash(BlockPos center, int x, int z) {
        int h = center.getX() * 73428767 ^ center.getZ() * 91227153 ^ x * 42317861 ^ z * 374761393;
        h ^= h >>> 13;
        h *= 1274126177;
        return h & Integer.MAX_VALUE;
    }
}
