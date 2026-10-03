package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.world.PocketDimension;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;

public final class PocketRenderEngine {
    private static ClientWorld cachedWorld;
    private static long cachedGameTime = Long.MIN_VALUE;
    private static BlockPos cachedPlayerPos;
    private static boolean cachedCorruption;

    private PocketRenderEngine() {}

    public static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        if (mc.level.dimension().equals(PocketDimension.POCKET_WORLD)) return true;
        return mc.level.dimension().equals(World.OVERWORLD) && inNetherCorruption(mc);
    }

    public static void applyFogColor(EntityViewRenderEvent.FogColors event) {
        if (!shouldApplyFog(Minecraft.getInstance())) return;
        event.setRed(0.78F);
        event.setGreen(0.08F);
        event.setBlue(0.03F);
    }

    public static void applyFogDistance(EntityViewRenderEvent.RenderFogEvent event) {
        if (!shouldApplyFog(Minecraft.getInstance())) return;
        RenderSystem.fogStart(1.5F);
        RenderSystem.fogEnd(24.0F);
        RenderSystem.setupNvFogDistance();
        event.setCanceled(true);
    }

    public static void applyFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!shouldApplyFog(Minecraft.getInstance())) return;
        event.setDensity(0.12F);
        event.setCanceled(true);
    }

    private static boolean shouldApplyFog(Minecraft mc) {
        if (mc.level == null) return false;
        if (mc.level.dimension().equals(PocketDimension.POCKET_WORLD)) return true;
        return mc.level.dimension().equals(World.OVERWORLD) && inNetherCorruption(mc);
    }

    private static boolean inNetherCorruption(Minecraft mc) {
        if (mc.level == null || mc.player == null) return false;
        ClientWorld world = mc.level;
        BlockPos center = mc.player.blockPosition();

        // Fog events can fire several times during one render tick. Reuse the
        // result instead of scanning the same 17x9x17 volume for every event.
        if (world == cachedWorld
                && world.getGameTime() == cachedGameTime
                && center.equals(cachedPlayerPos)) {
            return cachedCorruption;
        }

        int score = 0;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -4, -8), center.offset(8, 4, 8))) {
            Block block = world.getBlockState(pos).getBlock();
            if (isNetherCorruption(block)) {
                score++;
                if (score >= 8) {
                    cachedWorld = world;
                    cachedGameTime = world.getGameTime();
                    cachedPlayerPos = center.immutable();
                    cachedCorruption = true;
                    return true;
                }
            }
        }
        cachedWorld = world;
        cachedGameTime = world.getGameTime();
        cachedPlayerPos = center.immutable();
        cachedCorruption = score >= 8;
        return cachedCorruption;
    }

    private static boolean isNetherCorruption(Block block) {
        return block == Blocks.NETHERRACK
                || block == Blocks.SOUL_SAND
                || block == Blocks.SOUL_SOIL
                || block == Blocks.BLACKSTONE
                || block == Blocks.BASALT
                || block == Blocks.MAGMA_BLOCK
                || block == Blocks.LAVA
                || block == Blocks.FIRE;
    }
}
