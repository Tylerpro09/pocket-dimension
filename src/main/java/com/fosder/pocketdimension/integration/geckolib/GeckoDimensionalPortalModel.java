package com.fosder.pocketdimension.integration.geckolib;

import com.fosder.pocketdimension.PocketDimensionMod;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/** Recursos 3D y animación de la Grieta Dimensional. */
public class GeckoDimensionalPortalModel extends AnimatedGeoModel<GeckoDimensionalPortalTileEntity> {
    @Override
    public ResourceLocation getAnimationFileLocation(GeckoDimensionalPortalTileEntity entity) {
        return new ResourceLocation(PocketDimensionMod.MOD_ID, "animations/dimensional_portal.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(GeckoDimensionalPortalTileEntity entity) {
        return new ResourceLocation(PocketDimensionMod.MOD_ID, "geo/dimensional_portal.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(GeckoDimensionalPortalTileEntity entity) {
        return new ResourceLocation(PocketDimensionMod.MOD_ID, "textures/block/dimensional_portal.png");
    }
}
