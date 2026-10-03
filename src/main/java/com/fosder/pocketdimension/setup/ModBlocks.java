package com.fosder.pocketdimension.setup;

import com.fosder.pocketdimension.PocketDimensionMod;
import com.fosder.pocketdimension.block.DimensionalPortalBlock;
import com.fosder.pocketdimension.block.DarkDimensionalRiftBlock;
import com.fosder.pocketdimension.block.DimensionalTableBlock;
import com.fosder.pocketdimension.block.QuantumAirGeneratorBlock;
import com.fosder.pocketdimension.block.StabilizerMachineBlock;
import com.fosder.pocketdimension.integration.geckolib.GeckoDarkDimensionalRiftBlock;
import com.fosder.pocketdimension.integration.geckolib.GeckoDimensionalPortalBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PocketDimensionMod.MOD_ID);

    public static final RegistryObject<Block> DIMENSIONAL_TABLE = BLOCKS.register("dimensional_table", () ->
            new DimensionalTableBlock(AbstractBlock.Properties.of(Material.WOOD)
                    .strength(2.5F, 6.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistryObject<Block> STABILIZER_MACHINE = BLOCKS.register("stabilizer_machine", () ->
            new StabilizerMachineBlock(AbstractBlock.Properties.of(Material.METAL)
                    .strength(4.0F, 12.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 7)));

    public static final RegistryObject<Block> QUANTUM_AIR_GENERATOR = BLOCKS.register("quantum_air_generator", () ->
            new QuantumAirGeneratorBlock(AbstractBlock.Properties.of(Material.METAL)
                    .strength(3.5F, 9.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 4)));

    public static final RegistryObject<Block> STABILIZER_BATTERY_ORE = BLOCKS.register("stabilizer_battery_ore", () ->
            new Block(AbstractBlock.Properties.of(Material.STONE)
                    .strength(4.5F, 9.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 3)
                    .requiresCorrectToolForDrops()));


    public static final RegistryObject<Block> DIMENSIONAL_PORTAL = BLOCKS.register("dimensional_portal", () ->
            createDimensionalPortal(AbstractBlock.Properties.of(Material.GLASS)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .noCollission()
                    .noOcclusion()));

    public static final RegistryObject<Block> DARK_DIMENSIONAL_RIFT = BLOCKS.register("dark_dimensional_rift", () ->
            createDarkDimensionalRift(AbstractBlock.Properties.of(Material.GLASS)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 1)
                    .noCollission()
                    .noOcclusion()));

    private ModBlocks() {}

    private static Block createDimensionalPortal(AbstractBlock.Properties properties) {
        if (net.minecraftforge.fml.ModList.get().isLoaded("geckolib3")) {
            return new GeckoDimensionalPortalBlock(properties);
        }
        return new DimensionalPortalBlock(properties);
    }

    private static Block createDarkDimensionalRift(AbstractBlock.Properties properties) {
        if (net.minecraftforge.fml.ModList.get().isLoaded("geckolib3")) {
            return new GeckoDarkDimensionalRiftBlock(properties);
        }
        return new DarkDimensionalRiftBlock(properties);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
