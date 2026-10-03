package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.PocketDimensionMod;
import com.fosder.pocketdimension.integration.geckolib.GeckoAnimatedTileEntities;
import com.fosder.pocketdimension.integration.geckolib.GeckoDarkDimensionalRiftRenderer;
import com.fosder.pocketdimension.integration.geckolib.GeckoDimensionalPortalRenderer;
import com.fosder.pocketdimension.setup.ModBlocks;
import com.fosder.pocketdimension.setup.ModContainers;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;

@Mod.EventBusSubscriber(modid = PocketDimensionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PocketClientSetup {
    private PocketClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ScreenManager.register(
                    ModContainers.DIMENSIONAL_WORKBENCH.get(),
                    DimensionalWorkbenchScreen::new
            );
            ScreenManager.register(
                    ModContainers.STABILIZER_MACHINE.get(),
                    StabilizerMachineScreen::new
            );

            RenderTypeLookup.setRenderLayer(ModBlocks.DIMENSIONAL_PORTAL.get(), RenderType.translucent());

            if (ModList.get().isLoaded("geckolib3")) {
                ClientRegistry.bindTileEntityRenderer(
                        GeckoAnimatedTileEntities.DIMENSIONAL_PORTAL.get(),
                        GeckoDimensionalPortalRenderer::new
                );
                ClientRegistry.bindTileEntityRenderer(
                        GeckoAnimatedTileEntities.DARK_DIMENSIONAL_RIFT.get(),
                        GeckoDarkDimensionalRiftRenderer::new
                );
            }
        });
    }
}
