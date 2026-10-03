package com.fosder.pocketdimension.world;

import com.fosder.pocketdimension.util.PocketConfig;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public final class PocketMath {
    private PocketMath() {}

    /**
     * Centro determinista por UUID. No crea mundo alrededor: solo define dónde va el bolsillo.
     * Separación amplia para que un bolsillo no renderice otro con distancia normal.
     */
    public static BlockPos centerFor(UUID owner) {
        long a = owner.getMostSignificantBits();
        long b = owner.getLeastSignificantBits();
        int gx = Math.floorMod((int)(a ^ (a >>> 32)), 1000) - 500;
        int gz = Math.floorMod((int)(b ^ (b >>> 32)), 1000) - 500;
        return new BlockPos(gx * PocketConfig.getPocketSpacing(), PocketConfig.getFloorY(), gz * PocketConfig.getPocketSpacing());
    }

    public static boolean outsidePocket(BlockPos center, double x, double z) {
        double dx = x - center.getX();
        double dz = z - center.getZ();
        return Math.abs(dx) > PocketConfig.getHalfSize() - 1 || Math.abs(dz) > PocketConfig.getHalfSize() - 1;
    }
}
