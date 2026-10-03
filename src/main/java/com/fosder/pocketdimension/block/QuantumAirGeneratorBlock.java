package com.fosder.pocketdimension.block;

import com.fosder.pocketdimension.tile.QuantumAirGeneratorTileEntity;
import com.fosder.pocketdimension.util.PocketText;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class QuantumAirGeneratorBlock extends Block {
    public QuantumAirGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new QuantumAirGeneratorTileEntity();
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide) return ActionResultType.SUCCESS;

        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof QuantumAirGeneratorTileEntity) {
            QuantumAirGeneratorTileEntity generator = (QuantumAirGeneratorTileEntity) tile;
            PocketText.send(player, "message.pocketdimension.air_generator.title");
            PocketText.send(player, "message.pocketdimension.air_generator.energy", generator.getLastEnergySent());
            PocketText.send(player, "message.pocketdimension.air_generator.quality", generator.getAirQuality(), generator.getTreeScore());
            if (generator.getAirQuality() < 45) {
                PocketText.send(player, "message.pocketdimension.air_generator.danger");
            }
        }
        return ActionResultType.SUCCESS;
    }
}
