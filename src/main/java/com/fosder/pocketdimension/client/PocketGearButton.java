package com.fosder.pocketdimension.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class PocketGearButton extends Button {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "pocketdimension",
            "textures/gui/settings_gear.png"
    );

    public PocketGearButton(int x, int y, ITextComponent message, IPressable onPress, ITooltip onTooltip) {
        super(x, y, 18, 18, message, onPress, onTooltip);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        RenderSystem.enableDepthTest();
        blit(matrixStack, this.x, this.y, 0, 0, this.width, this.height, 16, 16);
        if (this.isHovered()) {
            this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
