package com.fosder.pocketdimension.tile;

import com.fosder.pocketdimension.setup.ModBlocks;
import com.fosder.pocketdimension.setup.ModTileEntities;
import com.fosder.pocketdimension.util.PocketConfig;
import com.fosder.pocketdimension.util.PocketStabilityData;
import com.fosder.pocketdimension.world.PocketOverworldHellEngine;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.UUID;

public class QuantumAirGeneratorTileEntity extends TileEntity implements ITickableTileEntity {
    private static final int CYCLE_TICKS = 20 * 5;
    private static final int SCAN_RADIUS = 8;
    private static final int STABILIZER_RADIUS = 12;
    private static final int BASE_ENERGY = 45;
    private static final int SAFE_TREE_SCORE = 24;

    private int airQuality = 100;
    private int treeScore = 0;
    private int lastEnergySent = 0;

    public QuantumAirGeneratorTileEntity() {
        super(ModTileEntities.QUANTUM_AIR_GENERATOR.get());
    }

    @Override
    public void tick() {
        if (this.level == null || this.level.isClientSide) return;
        if (this.level.getGameTime() % 5L == 0L) {
            emitRedBeacon((ServerWorld) this.level);
        }
        if (this.level.getGameTime() % CYCLE_TICKS != 0L) return;

        ServerWorld world = (ServerWorld) this.level;
        Sample sample = sampleCapsule(world, this.worldPosition);
        this.treeScore = sample.treeScore;
        this.airQuality = Math.max(0, Math.min(100, 25 + sample.treeScore * 3 + sample.water * 4 - BASE_ENERGY / 2));

        StabilizerMachineTileEntity stabilizer = findNearestOwnedStabilizer(world, this.worldPosition);
        this.lastEnergySent = 0;
        if (stabilizer != null && stabilizer.getOwnerUUID() != null) {
            ServerPlayerEntity owner = world.getServer().getPlayerList().getPlayer(stabilizer.getOwnerUUID());
            if (owner != null) {
                int generated = BASE_ENERGY + Math.min(50, sample.treeScore * 2 + sample.water * 3);
                if (this.airQuality < 45) {
                    generated /= 2;
                }
                int before = PocketStabilityData.getEnergy(owner);
                int after = PocketStabilityData.addEnergy(owner, generated);
                this.lastEnergySent = Math.max(0, after - before);
            }
        }

        if (this.airQuality < 45) {
            applyAirDamage(world, this.worldPosition, this.airQuality < 20);
        }

        if (this.airQuality <= PocketConfig.HELL_GAS_TRIGGER_AIR_QUALITY
                && this.treeScore < PocketConfig.HELL_GAS_TRIGGER_TREE_SCORE) {
            PocketOverworldHellEngine.activate(world, this.worldPosition, this.airQuality, this.treeScore);
        }

        setChanged();
    }

    private void emitRedBeacon(ServerWorld world) {
        int height = this.airQuality <= PocketConfig.HELL_GAS_TRIGGER_AIR_QUALITY ? 64 : 32;
        RedstoneParticleData core = new RedstoneParticleData(1.0F, 0.03F, 0.02F, 1.4F);
        RedstoneParticleData glow = new RedstoneParticleData(1.0F, 0.18F, 0.08F, 0.7F);

        for (int y = 1; y <= height; y += 2) {
            double pulse = Math.sin((world.getGameTime() + y) * 0.22D) * 0.08D;
            double x = this.worldPosition.getX() + 0.5D + pulse;
            double z = this.worldPosition.getZ() + 0.5D - pulse;
            world.sendParticles(core, x, this.worldPosition.getY() + y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            if (y % 6 == 1) {
                world.sendParticles(glow, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + y, this.worldPosition.getZ() + 0.5D, 3, 0.12D, 0.02D, 0.12D, 0.0D);
            }
        }
    }

    public int getAirQuality() {
        return airQuality;
    }

    public int getTreeScore() {
        return treeScore;
    }

    public int getLastEnergySent() {
        return lastEnergySent;
    }

    private static Sample sampleCapsule(ServerWorld world, BlockPos center) {
        Sample sample = new Sample();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-SCAN_RADIUS, -3, -SCAN_RADIUS), center.offset(SCAN_RADIUS, 6, SCAN_RADIUS))) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof LeavesBlock) sample.treeScore += 2;
            else if (isLog(block)) sample.treeScore += 3;
            else if (block instanceof BushBlock || block instanceof CropsBlock || block == Blocks.GRASS_BLOCK) sample.treeScore += 1;
            else if (block == Blocks.WATER) sample.water++;
        }
        return sample;
    }

    private static StabilizerMachineTileEntity findNearestOwnedStabilizer(ServerWorld world, BlockPos center) {
        StabilizerMachineTileEntity best = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-STABILIZER_RADIUS, -4, -STABILIZER_RADIUS), center.offset(STABILIZER_RADIUS, 4, STABILIZER_RADIUS))) {
            if (world.getBlockState(pos).getBlock() != ModBlocks.STABILIZER_MACHINE.get()) continue;
            TileEntity tile = world.getBlockEntity(pos);
            if (!(tile instanceof StabilizerMachineTileEntity)) continue;

            StabilizerMachineTileEntity stabilizer = (StabilizerMachineTileEntity) tile;
            UUID owner = stabilizer.getOwnerUUID();
            if (owner == null) continue;

            double distance = pos.distSqr(center);
            if (distance < bestDistance) {
                best = stabilizer;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static void applyAirDamage(ServerWorld world, BlockPos center, boolean severe) {
        AxisAlignedBB box = new AxisAlignedBB(center).inflate(SCAN_RADIUS + 1);
        for (ServerPlayerEntity player : world.getEntitiesOfClass(ServerPlayerEntity.class, box)) {
            if (player.isCreative()) continue;
            player.addEffect(new EffectInstance(Effects.CONFUSION, 120, 0, true, false));
            player.addEffect(new EffectInstance(Effects.WEAKNESS, 120, severe ? 1 : 0, true, false));
            player.hurt(DamageSource.DROWN, severe ? 2.0F : 1.0F);
        }
    }

    private static boolean isLog(Block block) {
        return block == Blocks.OAK_LOG || block == Blocks.BIRCH_LOG || block == Blocks.SPRUCE_LOG
                || block == Blocks.JUNGLE_LOG || block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG
                || block == Blocks.STRIPPED_OAK_LOG || block == Blocks.STRIPPED_BIRCH_LOG
                || block == Blocks.STRIPPED_SPRUCE_LOG || block == Blocks.STRIPPED_JUNGLE_LOG
                || block == Blocks.STRIPPED_ACACIA_LOG || block == Blocks.STRIPPED_DARK_OAK_LOG;
    }

    private static final class Sample {
        private int treeScore;
        private int water;
    }
}
