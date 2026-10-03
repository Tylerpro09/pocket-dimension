package com.fosder.pocketdimension.world;

import com.fosder.pocketdimension.util.PocketConfig;
import com.fosder.pocketdimension.util.PocketText;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/**
 * Simula fisica ambiental simple dentro del bolsillo.
 *
 * No intenta reemplazar el motor de Minecraft: agrega reglas realistas donde
 * vanilla no las tiene, como atmosfera local y soporte basico de bloques.
 */
public final class PocketPhysicsEngine {
    private PocketPhysicsEngine() {}

    public static void tickPlayer(ServerPlayerEntity player, ServerWorld world, BlockPos center, int stability) {
        if (player.isCreative()) return;

        if (player.tickCount % PocketConfig.ATMOSPHERE_CHECK_INTERVAL_TICKS == 0) {
            applyAtmosphere(player, world, stability);
        }
        if (player.tickCount % PocketConfig.THERMAL_CHECK_INTERVAL_TICKS == 0) {
            applyThermalStress(player, world);
        }
        if (player.tickCount % PocketConfig.STRUCTURE_CHECK_INTERVAL_TICKS == 0) {
            settleUnsupportedBlocks(world, player.blockPosition());
        }
    }

    private static void applyAtmosphere(ServerPlayerEntity player, ServerWorld world, int stability) {
        AtmosphereSample sample = sampleAtmosphere(world, player.blockPosition(), PocketConfig.ATMOSPHERE_SAMPLE_RADIUS);
        int oxygenScore = sample.plants * 2 + sample.water + stability / 8;

        if (oxygenScore < PocketConfig.MIN_OXYGEN_SCORE) {
            player.addEffect(new EffectInstance(Effects.CONFUSION, 120, 0, true, false));
            player.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 120, 0, true, false));
            player.hurt(DamageSource.DROWN, 1.0F);
            if (player.tickCount % 200 == 0) {
                PocketText.send(player, "message.pocketdimension.atmosphere.poor");
            }
        }
    }

    private static void applyThermalStress(ServerPlayerEntity player, ServerWorld world) {
        BlockPos pos = player.blockPosition();
        AtmosphereSample sample = sampleAtmosphere(world, pos, PocketConfig.THERMAL_SAMPLE_RADIUS);
        boolean exposedToSky = world.canSeeSky(pos.above());
        boolean coldNight = exposedToSky && !world.isDay();
        boolean overheating = sample.heat >= PocketConfig.OVERHEAT_SCORE;

        if (overheating) {
            player.addEffect(new EffectInstance(Effects.WEAKNESS, 100, 0, true, false));
            player.addEffect(new EffectInstance(Effects.HUNGER, 100, 0, true, false));
        } else if (coldNight && sample.heat == 0) {
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, 0, true, false));
            player.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 100, 0, true, false));
        }
    }

    private static AtmosphereSample sampleAtmosphere(ServerWorld world, BlockPos center, int radius) {
        AtmosphereSample sample = new AtmosphereSample();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 3, radius))) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (isOxygenSource(block)) sample.plants++;
            if (block == Blocks.WATER) sample.water++;
            if (isHeatSource(block)) sample.heat++;
        }
        return sample;
    }

    private static void settleUnsupportedBlocks(ServerWorld world, BlockPos origin) {
        int radius = PocketConfig.STRUCTURE_SAMPLE_RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -1, -radius), origin.offset(radius, 3, radius))) {
            BlockState state = world.getBlockState(pos);
            if (!canFall(state)) continue;
            if (isSupported(world, pos)) continue;

            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            FallingBlockEntity entity = new FallingBlockEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, state);
            world.addFreshEntity(entity);
            return;
        }
    }

    private static boolean canFall(BlockState state) {
        Block block = state.getBlock();
        if (state.isAir()) return false;
        if (block instanceof FallingBlock) return false;
        if (block instanceof BushBlock || block instanceof CropsBlock || block instanceof LeavesBlock) return false;
        if (block == Blocks.BEDROCK || block == Blocks.BARRIER || block == Blocks.OBSIDIAN) return false;
        if (block == Blocks.WATER || block == Blocks.LAVA || block == Blocks.FIRE) return false;
        return state.getMaterial().isSolid();
    }

    private static boolean isSupported(ServerWorld world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.below());
        return below.getMaterial().isSolid() || !world.isEmptyBlock(pos.below());
    }

    private static boolean isOxygenSource(Block block) {
        return block instanceof LeavesBlock
                || block instanceof BushBlock
                || block instanceof CropsBlock
                || block == Blocks.GRASS_BLOCK
                || block == Blocks.FERN
                || block == Blocks.LARGE_FERN
                || block == Blocks.VINE;
    }

    private static boolean isHeatSource(Block block) {
        return block == Blocks.FIRE
                || block == Blocks.CAMPFIRE
                || block == Blocks.SOUL_CAMPFIRE
                || block == Blocks.LAVA
                || block == Blocks.MAGMA_BLOCK;
    }

    private static final class AtmosphereSample {
        private int plants;
        private int water;
        private int heat;
    }
}
