package com.fosder.pocketdimension;

import com.fosder.pocketdimension.command.PocketCommands;
import com.fosder.pocketdimension.setup.ModBlocks;
import com.fosder.pocketdimension.setup.ModItems;
import com.fosder.pocketdimension.setup.ModContainers;
import com.fosder.pocketdimension.setup.ModTileEntities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import com.fosder.pocketdimension.util.PocketClientConfig;
import com.fosder.pocketdimension.util.PocketMapConfig;
import com.fosder.pocketdimension.network.PocketNetwork;

@Mod(PocketDimensionMod.MOD_ID)
public class PocketDimensionMod {
    public static final String MOD_ID = "pocketdimension";
    public static final String MODID = MOD_ID;

    public PocketDimensionMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PocketClientConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PocketMapConfig.COMMON_SPEC);
        PocketNetwork.register();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModContainers.register(modBus);
        ModTileEntities.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PocketCommands.register(event.getDispatcher());
    }
}
