package com.fosder.pocketdimension.world;

import com.fosder.pocketdimension.util.PocketConfig;
import com.fosder.pocketdimension.util.PocketText;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public final class PocketOverworldHellEngine {
    private static boolean active = false;
    private static boolean rapidMode = false;
    private static int spreadRadius = 0;
    private static long nextExpandTick = 0L;
    private static long netherClimateTick = 0L;
    private static BlockPos epicenter = BlockPos.ZERO;
    private static final ServerBossInfo GAS_BOSS_BAR = new ServerBossInfo(
            new TranslationTextComponent("bossbar.pocketdimension.hell_gas"),
            BossInfo.Color.RED,
            BossInfo.Overlay.PROGRESS
    );
    private static final Random RANDOM = new Random();

    private PocketOverworldHellEngine() {}

    public static void activate(ServerWorld sourceWorld, BlockPos sourcePos, int airQuality, int treeScore) {
        activate(sourceWorld, sourcePos, airQuality, treeScore, false);
    }

    public static void activate(ServerWorld sourceWorld, BlockPos sourcePos, int airQuality, int treeScore, boolean rapid) {
        if (sourceWorld == null || sourceWorld.getServer() == null) return;
        ServerWorld overworld = sourceWorld.getServer().getLevel(World.OVERWORLD);
        if (overworld == null) return;

        rapidMode = rapidMode || rapid;

        if (sourceWorld.dimension().equals(World.OVERWORLD)) {
            epicenter = sourcePos;
        }

        if (!active) {
            active = true;
            spreadRadius = PocketConfig.HELL_GAS_START_RADIUS;
            if (epicenter.equals(BlockPos.ZERO)) {
                epicenter = chooseEpicenter(overworld);
            }
            nextExpandTick = overworld.getGameTime() + expandInterval();
            netherClimateTick = overworld.getGameTime() + (rapidMode ? 20L : PocketConfig.HELL_GAS_DAY_PHASE_TICKS);
            for (ServerPlayerEntity player : overworld.players()) {
                PocketText.send(player, "message.pocketdimension.hell_gas.started");
                PocketText.send(player, "message.pocketdimension.hell_gas.survival_hint");
                PocketText.send(player, "message.pocketdimension.hell_gas.cause", airQuality, treeScore);
            }
        } else {
            spreadRadius = Math.min(PocketConfig.HELL_GAS_MAX_RADIUS, spreadRadius + expandBlocks());
        }

        if (rapidMode) {
            spreadRadius = Math.min(PocketConfig.HELL_GAS_MAX_RADIUS, Math.max(spreadRadius, 900));
            for (int i = 0; i < 24; i++) {
                corruptRapid(overworld);
                heatColdSurface(overworld);
            }
        }

        forceHellWeather(overworld, overworld.getGameTime());
    }

    public static void tick(ServerWorld world) {
        if (world == null || !world.dimension().equals(World.OVERWORLD)) return;
        if (!active) return;

        long now = world.getGameTime();
        if (now >= nextExpandTick) {
            spreadRadius = Math.min(PocketConfig.HELL_GAS_MAX_RADIUS, spreadRadius + expandBlocks());
            nextExpandTick = now + expandInterval();
            forceHellWeather(world, now);
            warnExpansion(world);
        }

        if (now % PocketConfig.HELL_GAS_WEATHER_INTERVAL_TICKS == 0L) {
            forceHellWeather(world, now);
            updateBossBar(world);
        }
        if (now % 5L == 0L) {
            emitEpicenterBeam(world);
        }
        if (now >= netherClimateTick && now % PocketConfig.HELL_GAS_LIGHTNING_INTERVAL_TICKS == 0L) {
            strikeHellLightning(world);
        }
        if (now % PocketConfig.HELL_GAS_EFFECT_INTERVAL_TICKS == 0L) {
            applyPlayerPressure(world);
        }
        if (now % PocketConfig.HELL_GAS_HEAT_INTERVAL_TICKS == 0L) {
            heatColdSurface(world);
        }
        if (now % (rapidMode ? 2L : PocketConfig.HELL_GAS_TERRAIN_INTERVAL_TICKS) == 0L) {
            corruptSmallPatch(world);
        }
    }

    public static void forceStop(ServerWorld world) {
        active = false;
        rapidMode = false;
        spreadRadius = 0;
        nextExpandTick = 0L;
        netherClimateTick = 0L;
        epicenter = BlockPos.ZERO;
        GAS_BOSS_BAR.removeAllPlayers();
        GAS_BOSS_BAR.setVisible(false);
        if (world != null && world.dimension().equals(World.OVERWORLD)) {
            world.setWeatherParameters(20 * 60 * 10, 0, false, false);
        }
    }

    private static long expandInterval() {
        return rapidMode ? 5L : PocketConfig.HELL_GAS_EXPAND_INTERVAL_TICKS;
    }

    private static int expandBlocks() {
        return rapidMode ? 180 : PocketConfig.HELL_GAS_EXPAND_BLOCKS;
    }

    private static void updateBossBar(ServerWorld world) {
        GAS_BOSS_BAR.setVisible(true);
        GAS_BOSS_BAR.setName(new TranslationTextComponent("bossbar.pocketdimension.hell_gas_progress", spreadRadius, PocketConfig.HELL_GAS_MAX_RADIUS));
        GAS_BOSS_BAR.setPercent(Math.max(0.0F, Math.min(1.0F, spreadRadius / (float) PocketConfig.HELL_GAS_MAX_RADIUS)));

        for (ServerPlayerEntity player : world.players()) {
            GAS_BOSS_BAR.addPlayer(player);
        }
    }

    private static void forceHellWeather(ServerWorld world, long now) {
        if (now < netherClimateTick) {
            forceDryDay(world);
        } else {
            forceNetherClimate(world);
        }
    }

    private static void forceDryDay(ServerWorld world) {
        world.setWeatherParameters(20 * 60 * 20, 0, false, false);
        long time = world.getDayTime();
        long day = time / 24000L;
        long noon = day * 24000L + 6000L;
        if (Math.abs(time - noon) > 1000L) {
            world.setDayTime(noon);
        }
    }

    private static void forceNetherClimate(ServerWorld world) {
        world.setWeatherParameters(20 * 60 * 20, 0, false, false);
        long time = world.getDayTime();
        long day = time / 24000L;
        long hotDusk = day * 24000L + 12500L;
        if (Math.abs(time - hotDusk) > 1500L) {
            world.setDayTime(hotDusk);
        }
    }

    private static void warnExpansion(ServerWorld world) {
        if (spreadRadius % 120 != 0) return;
        for (ServerPlayerEntity player : world.players()) {
            PocketText.send(player, "message.pocketdimension.hell_gas.expanding", spreadRadius);
        }
    }

    private static void emitEpicenterBeam(ServerWorld world) {
        RedstoneParticleData core = new RedstoneParticleData(1.0F, 0.02F, 0.01F, 1.8F);
        RedstoneParticleData glow = new RedstoneParticleData(1.0F, 0.12F, 0.04F, 0.9F);
        int height = 96;

        for (int y = 1; y <= height; y += 2) {
            double wave = Math.sin((world.getGameTime() + y) * 0.18D) * 0.10D;
            double x = epicenter.getX() + 0.5D + wave;
            double z = epicenter.getZ() + 0.5D - wave;
            world.sendParticles(core, x, epicenter.getY() + y, z, 2, 0.01D, 0.01D, 0.01D, 0.0D);
            if (y % 8 == 1) {
                world.sendParticles(glow, epicenter.getX() + 0.5D, epicenter.getY() + y, epicenter.getZ() + 0.5D, 8, 0.22D, 0.04D, 0.22D, 0.0D);
            }
        }
    }

    private static void strikeHellLightning(ServerWorld world) {
        BlockPos pos = findSurfaceInSpread(world);
        if (pos == null || RANDOM.nextInt(3) != 0) return;
        LightningBoltEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning == null) return;
        lightning.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        world.addFreshEntity(lightning);
    }

    private static BlockPos chooseEpicenter(ServerWorld overworld) {
        if (!overworld.players().isEmpty()) {
            return overworld.players().get(0).blockPosition();
        }
        return overworld.getSharedSpawnPos();
    }

    private static void applyPlayerPressure(ServerWorld world) {
        int radiusSq = spreadRadius * spreadRadius;
        for (ServerPlayerEntity player : world.players()) {
            if (player.isCreative()) continue;
            if (player.blockPosition().distSqr(epicenter) > radiusSq) continue;

            Shelter shelter = shelterAt(world, player.blockPosition());
            if (shelter == Shelter.DEEP) {
                player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 120, 0, true, false));
                continue;
            }

            int amplifier = shelter == Shelter.SHALLOW ? 0 : 1;
            player.addEffect(new EffectInstance(Effects.CONFUSION, 120, 0, true, false));
            player.addEffect(new EffectInstance(Effects.WEAKNESS, 120, amplifier, true, false));
            player.addEffect(new EffectInstance(Effects.HUNGER, 160, amplifier, true, false));

            if (shelter == Shelter.EXPOSED) {
                player.hurt(DamageSource.MAGIC, 2.0F);
                player.setSecondsOnFire(3);
                if (world.isEmptyBlock(player.blockPosition().above()) && RANDOM.nextInt(8) == 0) {
                    world.setBlock(player.blockPosition().above(), Blocks.FIRE.defaultBlockState(), 3);
                }
            } else {
                player.hurt(DamageSource.MAGIC, 0.5F);
            }
        }
    }

    private static void heatColdSurface(ServerWorld world) {
        int pulses = 8 + Math.min(32, spreadRadius / 200);
        for (int i = 0; i < pulses; i++) {
            BlockPos surface = findSurfaceInSpread(world);
            if (surface == null) continue;
            for (int j = 0; j < 4; j++) {
                BlockPos pos = surface.offset(RANDOM.nextInt(9) - 4, RANDOM.nextInt(4), RANDOM.nextInt(9) - 4);
                heatBlock(world, pos);
                heatBlock(world, pos.below());
            }
        }
    }

    private static void heatBlock(ServerWorld world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (block == Blocks.ICE || block == Blocks.FROSTED_ICE) {
            world.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        } else if (block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE) {
            world.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
        } else if (block == Blocks.WATER && RANDOM.nextInt(4) == 0) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void corruptSmallPatch(ServerWorld world) {
        if (rapidMode) {
            corruptRapid(world);
            return;
        }

        BlockPos base = findSurfaceInSpread(world);
        if (base == null) return;

        int patchSize = rapidMode ? 120 + Math.min(260, spreadRadius / 30) : 5 + Math.min(18, spreadRadius / 250);
        for (int i = 0; i < patchSize; i++) {
            int range = rapidMode ? 18 : 4;
            BlockPos pos = base.offset(RANDOM.nextInt(range * 2 + 1) - range, 0, RANDOM.nextInt(range * 2 + 1) - range);
            corruptBlock(world, pos);
            if (rapidMode) {
                corruptBlock(world, pos.above());
                corruptBlock(world, pos.below());
            }
        }
    }

    private static void corruptRapid(ServerWorld world) {
        corruptDiskAtSurface(world, epicenter, 14);

        for (ServerPlayerEntity player : world.players()) {
            if (player.blockPosition().distSqr(epicenter) <= spreadRadius * spreadRadius) {
                corruptDiskAtSurface(world, player.blockPosition(), 12);
            }
        }

        for (int i = 0; i < 10; i++) {
            BlockPos surface = findSurfaceInSpread(world);
            if (surface != null) {
                corruptDiskAtSurface(world, surface, 8);
            }
        }
    }

    private static void corruptDiskAtSurface(ServerWorld world, BlockPos center, int radius) {
        int radiusSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radiusSq) continue;
                if (RANDOM.nextInt(100) > 78) continue;

                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int y = Math.min(255, Math.max(1, world.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 0, z)).getY() - 1));
                BlockPos surface = new BlockPos(x, y, z);
                corruptBlock(world, surface);
                corruptBlock(world, surface.above());
                corruptBlock(world, surface.below());
                if (RANDOM.nextInt(4) == 0) {
                    corruptBlock(world, surface.below(2));
                }
            }
        }
    }

    private static void corruptBlock(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL) {
            world.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), 3);
            if (world.isEmptyBlock(pos.above()) && RANDOM.nextInt(3) == 0) {
                world.setBlock(pos.above(), Blocks.FIRE.defaultBlockState(), 3);
            }
        } else if (block == Blocks.SAND || block == Blocks.GRAVEL || block == Blocks.CLAY) {
            world.setBlock(pos, RANDOM.nextBoolean() ? Blocks.SOUL_SAND.defaultBlockState() : Blocks.SOUL_SOIL.defaultBlockState(), 3);
        } else if (block == Blocks.STONE || block == Blocks.ANDESITE || block == Blocks.GRANITE || block == Blocks.DIORITE) {
            if (rapidMode || RANDOM.nextInt(6) == 0) {
                world.setBlock(pos, RANDOM.nextBoolean() ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState(), 3);
            }
        } else if (block == Blocks.WATER && RANDOM.nextInt(2) == 0) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK || block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE || block == Blocks.FROSTED_ICE) {
            heatBlock(world, pos);
        } else if (block == Blocks.OAK_LEAVES || block == Blocks.BIRCH_LEAVES || block == Blocks.SPRUCE_LEAVES
                || block == Blocks.JUNGLE_LEAVES || block == Blocks.ACACIA_LEAVES || block == Blocks.DARK_OAK_LEAVES) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (block == Blocks.GRASS || block == Blocks.TALL_GRASS || block == Blocks.FERN || block == Blocks.LARGE_FERN
                || block == Blocks.DANDELION || block == Blocks.POPPY || block == Blocks.BLUE_ORCHID || block == Blocks.ALLIUM
                || block == Blocks.AZURE_BLUET || block == Blocks.RED_TULIP || block == Blocks.ORANGE_TULIP
                || block == Blocks.WHITE_TULIP || block == Blocks.PINK_TULIP || block == Blocks.OXEYE_DAISY
                || block == Blocks.CORNFLOWER || block == Blocks.LILY_OF_THE_VALLEY) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (block == Blocks.OAK_LOG || block == Blocks.BIRCH_LOG || block == Blocks.SPRUCE_LOG
                || block == Blocks.JUNGLE_LOG || block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG) {
            world.setBlock(pos, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 3);
            if (world.isEmptyBlock(pos.above()) && RANDOM.nextInt(2) == 0) {
                world.setBlock(pos.above(), Blocks.FIRE.defaultBlockState(), 3);
            }
        } else if (world.isEmptyBlock(pos) && world.getBlockState(pos.below()).getBlock() == Blocks.NETHERRACK && RANDOM.nextInt(4) == 0) {
            world.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    private static BlockPos findSurfaceInSpread(ServerWorld world) {
        int radius = Math.max(PocketConfig.HELL_GAS_START_RADIUS, spreadRadius);
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
            double distance = Math.sqrt(RANDOM.nextDouble()) * radius;
            int x = epicenter.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = epicenter.getZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = Math.min(255, Math.max(1, world.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 0, z)).getY() - 1));
            BlockPos pos = new BlockPos(x, y, z);
            if (!world.isEmptyBlock(pos)) return pos;
        }
        return null;
    }

    private static Shelter shelterAt(ServerWorld world, BlockPos pos) {
        if (pos.getY() <= PocketConfig.HELL_GAS_DEEP_SAFE_Y && !world.canSeeSky(pos)) {
            return Shelter.DEEP;
        }
        if (pos.getY() <= PocketConfig.HELL_GAS_PARTIAL_SAFE_Y && !world.canSeeSky(pos)) {
            return Shelter.SHALLOW;
        }
        if (!world.canSeeSky(pos)) {
            return Shelter.SHALLOW;
        }
        return Shelter.EXPOSED;
    }

    private enum Shelter {
        EXPOSED,
        SHALLOW,
        DEEP
    }
}
