package com.fosder.pocketdimension.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Grieta de Oscuridad Dimensional.
 *
 * No es un portal normal: aparece cuando la Máquina Estabilizadora se queda sin energía.
 * El motor de colapso la usa como punto visual de absorción del área.
 */
public class DarkDimensionalRiftBlock extends Block {
    public DarkDimensionalRiftBlock(AbstractBlock.Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof PlayerEntity) {
            entity.hurt(DamageSource.MAGIC, 2.0F);
        }
        super.entityInside(state, world, pos, entity);
    }
}
