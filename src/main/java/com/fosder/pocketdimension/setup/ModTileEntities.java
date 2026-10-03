package com.fosder.pocketdimension.setup;

import com.fosder.pocketdimension.PocketDimensionMod;
import com.fosder.pocketdimension.integration.geckolib.GeckoAnimatedTileEntities;
import com.fosder.pocketdimension.tile.QuantumAirGeneratorTileEntity;
import com.fosder.pocketdimension.tile.StabilizerMachineTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Registro de TileEntities del mod. */
public final class ModTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, PocketDimensionMod.MOD_ID);

    public static final RegistryObject<TileEntityType<StabilizerMachineTileEntity>> STABILIZER_MACHINE = TILE_ENTITIES.register(
            "stabilizer_machine",
            () -> TileEntityType.Builder.of(StabilizerMachineTileEntity::new, ModBlocks.STABILIZER_MACHINE.get()).build(null)
    );

    public static final RegistryObject<TileEntityType<QuantumAirGeneratorTileEntity>> QUANTUM_AIR_GENERATOR = TILE_ENTITIES.register(
            "quantum_air_generator",
            () -> TileEntityType.Builder.of(QuantumAirGeneratorTileEntity::new, ModBlocks.QUANTUM_AIR_GENERATOR.get()).build(null)
    );

    private ModTileEntities() {}

    public static void register(IEventBus bus) {
        if (ModList.get().isLoaded("geckolib3")) {
            GeckoAnimatedTileEntities.register(TILE_ENTITIES);
        }
        TILE_ENTITIES.register(bus);
    }
}
