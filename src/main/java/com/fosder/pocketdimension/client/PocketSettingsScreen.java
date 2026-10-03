package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.util.PocketClientConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.Language;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.resource.VanillaResourceType;

/**
 * Ajustes propios del mod. No reemplaza ni abre el menu de idiomas de Minecraft.
 */
public class PocketSettingsScreen extends Screen {
    private final Screen parent;
    private Button fogButton;
    private Button detailsButton;
    private Button tooltipsButton;

    public PocketSettingsScreen(Screen parent) {
        super(new TranslationTextComponent("screen.pocketdimension.settings_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int panelTop = 64;
        int leftColumn = center - 175;
        int rightColumn = center + 25;

        this.addButton(new Button(
                leftColumn,
                panelTop + 30,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.spanish"),
                button -> selectLanguage("es_es")
        ));
        this.addButton(new Button(
                leftColumn,
                panelTop + 54,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.english"),
                button -> selectLanguage("en_us")
        ));
        this.addButton(new Button(
                leftColumn,
                panelTop + 78,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.french"),
                button -> selectLanguage("fr_fr")
        ));
        this.addButton(new Button(
                leftColumn,
                panelTop + 102,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.german"),
                button -> selectLanguage("de_de")
        ));
        this.addButton(new Button(
                leftColumn,
                panelTop + 126,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.portuguese"),
                button -> selectLanguage("pt_br")
        ));

        this.fogButton = addToggleButton(
                rightColumn,
                panelTop + 30,
                "button.pocketdimension.dimensional_fog",
                PocketClientConfig.DIMENSIONAL_FOG
        );
        this.detailsButton = addToggleButton(
                rightColumn,
                panelTop + 56,
                "button.pocketdimension.detailed_info",
                PocketClientConfig.DETAILED_MACHINE_INFO
        );
        this.tooltipsButton = addToggleButton(
                rightColumn,
                panelTop + 82,
                "button.pocketdimension.gui_tooltips",
                PocketClientConfig.GUI_TOOLTIPS
        );
        this.addButton(new Button(
                rightColumn,
                panelTop + 106,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.tutorial"),
                button -> this.minecraft.setScreen(new PocketTutorialScreen(this))
        ));
        this.addButton(new Button(
                rightColumn,
                panelTop + 130,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.map"),
                button -> this.minecraft.setScreen(new PocketMapEditorScreen(this))
        ));
        this.addButton(new Button(
                rightColumn,
                panelTop + 154,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.translation_credits"),
                button -> this.minecraft.setScreen(new PocketCommunityTranslationsScreen(this))
        ));

        this.addButton(new Button(
                center - 175,
                this.height - 50,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.defaults"),
                button -> {
                    PocketClientConfig.resetToDefaults();
                    refreshToggleLabels();
                }
        ));
        this.addButton(new Button(
                center + 25,
                this.height - 50,
                150,
                20,
                new TranslationTextComponent("button.pocketdimension.back"),
                button -> this.minecraft.setScreen(this.parent)
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        int center = this.width / 2;
        int panelTop = 64;
        int panelBottom = 242;
        int leftColumn = center - 185;
        int rightColumn = center + 15;

        drawCenteredString(matrixStack, this.font, this.title, center, 22, 0xFFFFFF);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.advanced_description"),
                center, 42, 0xB8C6D9);

        drawPanel(matrixStack, leftColumn, panelTop, 180, panelBottom - panelTop, 0xFF16243A, 0xFF0A1220);
        drawPanel(matrixStack, rightColumn, panelTop, 180, panelBottom - panelTop, 0xFF16243A, 0xFF0A1220);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.language_section"),
                leftColumn + 90, panelTop + 10, 0x7EEBFF);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.visual_section"),
                rightColumn + 90, panelTop + 10, 0xC6A8FF);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.language_description"),
                leftColumn + 90, panelTop + 20, 0x8EA0C4);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private Button addToggleButton(int x, int y, String translationKey, ForgeConfigSpec.BooleanValue value) {
        return this.addButton(new Button(
                x,
                y,
                150,
                20,
                toggleLabel(translationKey, value.get()),
                button -> {
                    value.set(!value.get());
                    button.setMessage(toggleLabel(translationKey, value.get()));
                }
        ));
    }

    private ITextComponent toggleLabel(String translationKey, boolean enabled) {
        return new TranslationTextComponent(
                translationKey,
                new TranslationTextComponent(enabled
                        ? "value.pocketdimension.on"
                        : "value.pocketdimension.off")
        );
    }

    private void refreshToggleLabels() {
        this.fogButton.setMessage(toggleLabel(
                "button.pocketdimension.dimensional_fog",
                PocketClientConfig.DIMENSIONAL_FOG.get()
        ));
        this.detailsButton.setMessage(toggleLabel(
                "button.pocketdimension.detailed_info",
                PocketClientConfig.DETAILED_MACHINE_INFO.get()
        ));
        this.tooltipsButton.setMessage(toggleLabel(
                "button.pocketdimension.gui_tooltips",
                PocketClientConfig.GUI_TOOLTIPS.get()
        ));
    }

    private void drawPanel(MatrixStack matrixStack, int x, int y, int width, int height, int border, int fillColor) {
        fill(matrixStack, x, y, x + width, y + height, border);
        fill(matrixStack, x + 2, y + 2, x + width - 2, y + height - 2, fillColor);
    }

    private void selectLanguage(String code) {
        Language language = this.minecraft.getLanguageManager().getLanguage(code);
        if (language == null) {
            return;
        }

        this.minecraft.getLanguageManager().setSelected(language);
        this.minecraft.options.languageCode = code;
        ForgeHooksClient.refreshResources(this.minecraft, VanillaResourceType.LANGUAGES);
        this.minecraft.options.save();
    }
}
