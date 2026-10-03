package com.fosder.pocketdimension.world;

import com.fosder.pocketdimension.PocketDimensionMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

public final class PocketDimension {
    public static final RegistryKey<World> POCKET_WORLD = RegistryKey.create(
            Registry.DIMENSION_REGISTRY,
            new ResourceLocation(PocketDimensionMod.MOD_ID, "pocket_world")
    );

    private PocketDimension() {}

    public static boolean isPocketWorld(World world) {
        return world != null && world.dimension().equals(POCKET_WORLD);
    }
}
