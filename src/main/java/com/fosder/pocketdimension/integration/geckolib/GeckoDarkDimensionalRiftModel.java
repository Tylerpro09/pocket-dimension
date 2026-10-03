package com.fosder.pocketdimension.integration.geckolib;

import com.fosder.pocketdimension.PocketDimensionMod;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/** Recursos 3D y animación de la Grieta Oscura durante el colapso. */
public class GeckoDarkDimensionalRiftModel extends AnimatedGeoModel<GeckoDarkDimensionalRiftTileEntity> {
    @Override
    public ResourceLocation getAnimationFileLocation(GeckoDarkDimensionalRiftTileEntity entity) {
        return new ResourceLocation(PocketDimensionMod.MOD_ID, "animations/dark_dimensional_rift.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(GeckoDarkDimensionalRiftTileEntity entity) {
        return new ResourceLocation(PocketDimensionMod.MOD_ID, "geo/dark_dimensional_rift.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(GeckoDarkDimensionalRiftTileEntity entity) {
        return new ResourceLocation(PocketDimensionMod.MOD_ID, "textures/block/dark_dimensional_rift.png");
    }
}
