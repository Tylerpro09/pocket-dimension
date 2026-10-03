package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.PocketDimensionMod;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PocketDimensionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PocketMenuEvents {
    private PocketMenuEvents() {
    }

    @SubscribeEvent
    public static void onMenuInit(GuiScreenEvent.InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if (!(screen instanceof MainMenuScreen) && !(screen instanceof IngameMenuScreen)) {
            return;
        }

        int y = 4;
        for (Widget widget : event.getWidgetList()) {
            y = Math.max(y, widget.y + widget.getHeight() + 4);
        }

        y = Math.min(y, screen.height - 24);
        event.addWidget(new PocketGearButton(
                screen.width / 2 - 9,
                y,
                new TranslationTextComponent("button.pocketdimension.settings"),
                button -> screen.getMinecraft().setScreen(new PocketSettingsScreen(screen)),
                (button, matrixStack, mouseX, mouseY) -> screen.renderTooltip(
                        matrixStack,
                        new TranslationTextComponent("button.pocketdimension.settings"),
                        mouseX,
                        mouseY
                )
        ));
    }
}
