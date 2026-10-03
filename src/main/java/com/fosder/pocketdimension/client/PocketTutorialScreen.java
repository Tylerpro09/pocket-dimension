package com.fosder.pocketdimension.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.List;

/**
 * Tutorial traducible del mod. El contenido se mantiene en los archivos de idioma
 * para que el mismo panel funcione en todos los idiomas disponibles.
 */
public class PocketTutorialScreen extends Screen {
    private static final int PAGE_COUNT = 4;
    private final Screen parent;
    private int page;
    private Button previousButton;
    private Button nextButton;

    public PocketTutorialScreen(Screen parent) {
        this(parent, 0);
    }

    private PocketTutorialScreen(Screen parent, int page) {
        super(new TranslationTextComponent("screen.pocketdimension.tutorial_title"));
        this.parent = parent;
        this.page = Math.max(0, Math.min(PAGE_COUNT - 1, page));
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int y = this.height - 34;

        this.previousButton = this.addButton(new Button(
                center - 155,
                y,
                100,
                20,
                new TranslationTextComponent("button.pocketdimension.tutorial_previous"),
                button -> changePage(-1)
        ));
        this.addButton(new Button(
                center - 50,
                y,
                100,
                20,
                new TranslationTextComponent("button.pocketdimension.back"),
                button -> this.minecraft.setScreen(this.parent)
        ));
        this.nextButton = this.addButton(new Button(
                center + 55,
                y,
                100,
                20,
                new TranslationTextComponent("button.pocketdimension.tutorial_next"),
                button -> changePage(1)
        ));
        refreshNavigation();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        int center = this.width / 2;
        int panelX = Math.max(20, center - 250);
        int panelY = 48;
        int panelWidth = Math.min(500, this.width - panelX * 2);
        int panelHeight = this.height - 94;

        drawCenteredString(matrixStack, this.font, this.title, center, 18, 0xFFFFFF);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.tutorial_page_indicator", this.page + 1, PAGE_COUNT),
                center, 32, 0x8EA0C4);

        fill(matrixStack, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF1DEBFF);
        fill(matrixStack, panelX + 2, panelY + 2, panelX + panelWidth - 2, panelY + panelHeight - 2, 0xFF0A1220);
        drawCenteredString(matrixStack, this.font, pageTitle(), center, panelY + 14, 0x7EEBFF);

        int lineY = panelY + 36;
        int textWidth = panelWidth - 36;
        for (ITextComponent line : pageLines()) {
            List<IReorderingProcessor> wrappedLines = this.font.split(line, textWidth);
            for (IReorderingProcessor wrappedLine : wrappedLines) {
                this.font.draw(matrixStack, wrappedLine, panelX + 18, lineY, 0xD8F6FF);
                lineY += 10;
            }
            lineY += 5;
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void changePage(int amount) {
        this.page = Math.max(0, Math.min(PAGE_COUNT - 1, this.page + amount));
        refreshNavigation();
    }

    private void refreshNavigation() {
        if (this.previousButton != null) {
            this.previousButton.active = this.page > 0;
        }
        if (this.nextButton != null) {
            this.nextButton.active = this.page < PAGE_COUNT - 1;
        }
    }

    private ITextComponent pageTitle() {
        return new TranslationTextComponent("screen.pocketdimension.tutorial_page" + (this.page + 1) + "_title");
    }

    private List<ITextComponent> pageLines() {
        switch (this.page) {
            case 0:
                return Arrays.asList(
                        line("screen.pocketdimension.tutorial_page1_line1"),
                        line("screen.pocketdimension.tutorial_page1_line2"),
                        line("screen.pocketdimension.tutorial_page1_line3"),
                        line("screen.pocketdimension.tutorial_page1_line4"),
                        line("screen.pocketdimension.tutorial_page1_line5")
                );
            case 1:
                return Arrays.asList(
                        line("screen.pocketdimension.tutorial_page2_line1"),
                        line("screen.pocketdimension.tutorial_page2_line2"),
                        line("screen.pocketdimension.tutorial_page2_line3"),
                        line("screen.pocketdimension.tutorial_page2_line4"),
                        line("screen.pocketdimension.tutorial_page2_line5")
                );
            case 2:
                return Arrays.asList(
                        line("screen.pocketdimension.tutorial_page3_line1"),
                        line("screen.pocketdimension.tutorial_page3_line2"),
                        line("screen.pocketdimension.tutorial_page3_line3"),
                        line("screen.pocketdimension.tutorial_page3_line4"),
                        line("screen.pocketdimension.tutorial_page3_line5")
                );
            default:
                return Arrays.asList(
                        line("screen.pocketdimension.tutorial_page4_line1"),
                        line("screen.pocketdimension.tutorial_page4_line2"),
                        line("screen.pocketdimension.tutorial_page4_line3"),
                        line("screen.pocketdimension.tutorial_page4_line4"),
                        line("screen.pocketdimension.tutorial_page4_line5")
                );
        }
    }

    private ITextComponent line(String key) {
        return new TranslationTextComponent(key);
    }
}
