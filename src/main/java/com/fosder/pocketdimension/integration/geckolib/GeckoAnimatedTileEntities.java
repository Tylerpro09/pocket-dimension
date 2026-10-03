package com.fosder.pocketdimension.integration.geckolib;

import com.fosder.pocketdimension.setup.ModBlocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Registros opcionales de las grietas animadas. Esta clase solo se carga cuando
 * GeckoLib está instalado, por lo que el mod conserva un modelo vanilla de
 * respaldo si el jugador no usa la dependencia visual.
 */
public final class GeckoAnimatedTileEntities {
    public static RegistryObject<TileEntityType<GeckoDimensionalPortalTileEntity>> DIMENSIONAL_PORTAL;
    public static RegistryObject<TileEntityType<GeckoDarkDimensionalRiftTileEntity>> DARK_DIMENSIONAL_RIFT;

    private static boolean registered;

    private GeckoAnimatedTileEntities() {}

    public static void register(DeferredRegister<TileEntityType<?>> tiles) {
        if (registered) {
            return;
        }
        registered = true;

        DIMENSIONAL_PORTAL = tiles.register(
                "dimensional_portal_animation",
                () -> TileEntityType.Builder.of(
                        GeckoDimensionalPortalTileEntity::new,
                        ModBlocks.DIMENSIONAL_PORTAL.get()
                ).build(null)
        );
        DARK_DIMENSIONAL_RIFT = tiles.register(
                "dark_dimensional_rift_animation",
                () -> TileEntityType.Builder.of(
                        GeckoDarkDimensionalRiftTileEntity::new,
                        ModBlocks.DARK_DIMENSIONAL_RIFT.get()
                ).build(null)
        );
    }
}
