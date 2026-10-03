package com.fosder.pocketdimension.integration.geckolib;

import com.fosder.pocketdimension.block.DarkDimensionalRiftBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

/** Variante de la grieta oscura que añade un TileEntity animable. */
public class GeckoDarkDimensionalRiftBlock extends DarkDimensionalRiftBlock {
    public GeckoDarkDimensionalRiftBlock(AbstractBlock.Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new GeckoDarkDimensionalRiftTileEntity();
    }
}
