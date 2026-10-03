package com.fosder.pocketdimension.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.UUID;

/**
 * Fachada compatible. El motor real ahora está en PocketGenerationEngine.
 */
public final class PocketWorldBuilder {
    private PocketWorldBuilder() {}

    public static BlockPos ensurePocket(ServerWorld world, UUID owner) {
        return PocketGenerationEngine.preparePocket(world, owner);
    }

    public static void rebuildPocket(ServerWorld world, UUID owner) {
        PocketGenerationEngine.rebuildPocket(world, owner);
    }
}
