package com.fosder.pocketdimension.integration.geckolib;

import com.fosder.pocketdimension.block.DimensionalPortalBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

/** Variante de la grieta dimensional que añade un TileEntity animable. */
public class GeckoDimensionalPortalBlock extends DimensionalPortalBlock {
    public GeckoDimensionalPortalBlock(AbstractBlock.Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new GeckoDimensionalPortalTileEntity();
    }
}
