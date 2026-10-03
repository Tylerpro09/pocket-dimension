package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.network.MaterialShopPacket;
import com.fosder.pocketdimension.network.PocketNetwork;
import com.fosder.pocketdimension.util.PocketMaterialShop;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

/** Tienda visual del editor: materiales por energía, sin cambiar a creativo. */
public class PocketMaterialShopScreen extends Screen {
    private final Screen parent;

    public PocketMaterialShopScreen(Screen parent) {
        super(new TranslationTextComponent("screen.pocketdimension.shop_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int leftPanel = center - 190;
        int rightPanel = center + 10;
        int firstY = 72;

        for (int index = 0; index < PocketMaterialShop.ENTRY_IDS.length; index++) {
            int entryId = PocketMaterialShop.ENTRY_IDS[index];
            int column = index % 2;
            int row = index / 2;
            int x = (column == 0 ? leftPanel : rightPanel) + 24;
            int y = firstY + row * 35;
            this.addButton(new Button(
                    x, y, 156, 20,
                    new TranslationTextComponent("button.pocketdimension.shop_buy",
                            new TranslationTextComponent(PocketMaterialShop.labelKey(entryId)),
                            PocketMaterialShop.createStack(entryId).getCount(),
                            PocketMaterialShop.cost(entryId)),
                    button -> PocketNetwork.sendToServer(new MaterialShopPacket(entryId))
            ));
        }

        this.addButton(new Button(
                center - 50, this.height - 32, 100, 20,
                new TranslationTextComponent("button.pocketdimension.shop_back"),
                button -> this.minecraft.setScreen(this.parent)
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        int center = this.width / 2;
        int leftPanel = center - 190;
        int rightPanel = center + 10;
        int panelTop = 58;
        int panelHeight = 205;

        drawCenteredString(matrixStack, this.font, this.title, center, 16, 0xFFFFFF);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.shop_description"),
                center, 31, 0xB8C6D9);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.shop_energy_rule"),
                center, 44, 0x8FE6A0);

        drawPanel(matrixStack, leftPanel, panelTop, 180, panelHeight, 0xFF16243A, 0xFF0A1220);
        drawPanel(matrixStack, rightPanel, panelTop, 180, panelHeight, 0xFF16243A, 0xFF0A1220);

        for (int index = 0; index < PocketMaterialShop.ENTRY_IDS.length; index++) {
            int entryId = PocketMaterialShop.ENTRY_IDS[index];
            int column = index % 2;
            int row = index / 2;
            int x = (column == 0 ? leftPanel : rightPanel);
            int y = 72 + row * 35;
            ItemStack stack = PocketMaterialShop.createStack(entryId);
            this.itemRenderer.renderAndDecorateItem(stack, x + 3, y + 2);
            this.itemRenderer.renderGuiItemDecorations(this.font, stack, x + 3, y + 2);
        }

        List<IReorderingProcessor> lines = this.font.split(
                new TranslationTextComponent("screen.pocketdimension.shop_footer"),
                this.width - 40
        );
        int footerY = this.height - 55;
        for (IReorderingProcessor line : lines) {
            this.font.draw(matrixStack, line, 20, footerY, 0xE6C77A);
            footerY += 10;
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void drawPanel(MatrixStack matrixStack, int x, int y, int width, int height, int border, int fillColor) {
        fill(matrixStack, x, y, x + width, y + height, border);
        fill(matrixStack, x + 2, y + 2, x + width - 2, y + height - 2, fillColor);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
