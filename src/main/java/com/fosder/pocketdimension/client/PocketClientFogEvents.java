package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.PocketDimensionMod;
import com.fosder.pocketdimension.util.PocketClientConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Hook visual exclusivo del bolsillo. El motor real está en PocketRenderEngine.
 */
@Mod.EventBusSubscriber(modid = PocketDimensionMod.MOD_ID, value = Dist.CLIENT)
public final class PocketClientFogEvents {
    private PocketClientFogEvents() {}

    @SubscribeEvent
    public static void onFogColor(EntityViewRenderEvent.FogColors event) {
        if (PocketClientConfig.DIMENSIONAL_FOG.get() && PocketRenderEngine.active()) {
            PocketRenderEngine.applyFogColor(event);
        }
    }

    @SubscribeEvent
    public static void onRenderFog(EntityViewRenderEvent.RenderFogEvent event) {
        if (PocketClientConfig.DIMENSIONAL_FOG.get() && PocketRenderEngine.active()) {
            PocketRenderEngine.applyFogDistance(event);
        }
    }

    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (PocketClientConfig.DIMENSIONAL_FOG.get() && PocketRenderEngine.active()) {
            PocketRenderEngine.applyFogDensity(event);
        }
    }
}
