package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.util.PocketMapConfig;
import com.fosder.pocketdimension.network.EditorModePacket;
import com.fosder.pocketdimension.network.EditorTravelPacket;
import com.fosder.pocketdimension.network.MapConfigPacket;
import com.fosder.pocketdimension.network.PocketNetwork;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

/**
 * Editor visual de los valores que definen el mapa del bolsillo.
 *
 * La pantalla solo escribe valores acotados de la configuración COMMON; no
 * permite inyectar comandos ni modificar archivos arbitrarios del jugador.
 */
public class PocketMapEditorScreen extends Screen {
    private final Screen parent;
    private final boolean editorMode;

    private TextFieldWidget pocketSize;
    private TextFieldWidget pocketSpacing;
    private TextFieldWidget floorY;
    private TextFieldWidget islandRadius;
    private TextFieldWidget islandThickness;
    private TextFieldWidget borderHeight;
    private TextFieldWidget riftDuration;
    private TextFieldWidget riftWidth;
    private TextFieldWidget riftHeight;
    private TextFieldWidget voidDamage;
    private ITextComponent status;

    public PocketMapEditorScreen(Screen parent) {
        this(parent, false);
    }

    public PocketMapEditorScreen(Screen parent, boolean editorMode) {
        super(new TranslationTextComponent("screen.pocketdimension.map_title"));
        this.parent = parent;
        this.editorMode = editorMode;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int left = center - 175;
        int right = center + 25;
        int row = 74;
        int gap = 36;

        this.pocketSize = addField(left, row, "screen.pocketdimension.map.pocket_size", Integer.toString(PocketMapConfig.pocketSize()), false);
        this.pocketSpacing = addField(right, row, "screen.pocketdimension.map.pocket_spacing", Integer.toString(PocketMapConfig.pocketSpacing()), false);
        this.floorY = addField(left, row + gap, "screen.pocketdimension.map.floor_y", Integer.toString(PocketMapConfig.floorY()), false);
        this.islandRadius = addField(right, row + gap, "screen.pocketdimension.map.island_radius", Integer.toString(PocketMapConfig.islandRadius()), false);
        this.islandThickness = addField(left, row + gap * 2, "screen.pocketdimension.map.island_thickness", Integer.toString(PocketMapConfig.islandThickness()), false);
        this.borderHeight = addField(right, row + gap * 2, "screen.pocketdimension.map.border_height", Integer.toString(PocketMapConfig.borderHeight()), false);
        this.riftDuration = addField(left, row + gap * 3, "screen.pocketdimension.map.rift_duration", Integer.toString(PocketMapConfig.RIFT_DURATION_SECONDS.get()), false);
        this.riftWidth = addField(right, row + gap * 3, "screen.pocketdimension.map.rift_width", Integer.toString(PocketMapConfig.riftWidth()), false);
        this.riftHeight = addField(left, row + gap * 4, "screen.pocketdimension.map.rift_height", Integer.toString(PocketMapConfig.riftHeight()), false);
        this.voidDamage = addField(right, row + gap * 4, "screen.pocketdimension.map.void_damage", Double.toString(PocketMapConfig.VOID_SHEAR_DAMAGE.get()), true);

        if (this.editorMode) {
            this.addButton(new Button(
                    center - 175,
                    this.height - 56,
                    100,
                    20,
                    new TranslationTextComponent("button.pocketdimension.map_shop"),
                    button -> this.minecraft.setScreen(new PocketMaterialShopScreen(this))
            ));
            this.addButton(new Button(
                    center - 65,
                    this.height - 56,
                    130,
                    20,
                    new TranslationTextComponent("button.pocketdimension.map_save"),
                    button -> saveValues()
            ));
            this.addButton(new Button(
                    center + 75,
                    this.height - 56,
                    100,
                    20,
                    new TranslationTextComponent("button.pocketdimension.map_defaults"),
                    button -> resetValues()
            ));
            this.addButton(new Button(
                    center - 65,
                    this.height - 32,
                    130,
                    20,
                    new TranslationTextComponent("button.pocketdimension.map_enter_pocket"),
                    button -> {
                        PocketNetwork.sendToServer(new EditorTravelPacket());
                        this.minecraft.setScreen(null);
                    }
            ));
        } else {
            this.addButton(new Button(
                    center - 175,
                    this.height - 32,
                    100,
                    20,
                    new TranslationTextComponent("button.pocketdimension.map_save"),
                    button -> saveValues()
            ));
            this.addButton(new Button(
                    center - 65,
                    this.height - 32,
                    130,
                    20,
                    new TranslationTextComponent("button.pocketdimension.map_defaults"),
                    button -> resetValues()
            ));
        }
        this.addButton(new Button(
                center + 75,
                this.height - 32,
                100,
                20,
                new TranslationTextComponent(this.editorMode
                        ? "button.pocketdimension.map_exit_editor"
                        : "button.pocketdimension.back"),
                button -> closeEditor()
        ));

    }

    private TextFieldWidget addField(int x, int y, String labelKey, String value, boolean decimal) {
        TextFieldWidget field = new TextFieldWidget(
                this.font,
                x,
                y,
                150,
                20,
                new TranslationTextComponent(labelKey)
        );
        field.setMaxLength(8);
        field.setValue(value);
        field.setFilter(text -> decimal
                ? text.matches("[0-9]*([\\.,][0-9]*)?")
                : text.matches("[0-9]*"));
        this.addWidget(field);
        return field;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        int center = this.width / 2;
        int left = center - 185;
        int right = center + 15;
        int panelTop = 50;
        int panelBottom = this.height - 82;

        drawCenteredString(matrixStack, this.font, this.title, center, 16, 0xFFFFFF);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.map_description"),
                center, 31, 0xB8C6D9);
        if (this.editorMode) {
            drawCenteredString(matrixStack, this.font,
                    new TranslationTextComponent("screen.pocketdimension.map_editor_mode"),
                    center, 43, 0x8FE6A0);
        }

        drawPanel(matrixStack, left, panelTop, 180, panelBottom - panelTop, 0xFF16243A, 0xFF0A1220);
        drawPanel(matrixStack, right, panelTop, 180, panelBottom - panelTop, 0xFF16243A, 0xFF0A1220);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.map_layout_section"),
                left + 90, panelTop + 10, 0x7EEBFF);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.map_rifts_section"),
                right + 90, panelTop + 10, 0xC6A8FF);

        drawLabel(matrixStack, "screen.pocketdimension.map.pocket_size", left, 62);
        drawLabel(matrixStack, "screen.pocketdimension.map.floor_y", left, 98);
        drawLabel(matrixStack, "screen.pocketdimension.map.island_thickness", left, 134);
        drawLabel(matrixStack, "screen.pocketdimension.map.rift_duration", left, 170);
        drawLabel(matrixStack, "screen.pocketdimension.map.rift_height", left, 206);
        drawLabel(matrixStack, "screen.pocketdimension.map.pocket_spacing", right, 62);
        drawLabel(matrixStack, "screen.pocketdimension.map.island_radius", right, 98);
        drawLabel(matrixStack, "screen.pocketdimension.map.border_height", right, 134);
        drawLabel(matrixStack, "screen.pocketdimension.map.rift_width", right, 170);
        drawLabel(matrixStack, "screen.pocketdimension.map.void_damage", right, 206);

        List<IReorderingProcessor> warningLines = this.font.split(
                new TranslationTextComponent("screen.pocketdimension.map_warning"),
                this.width - 40
        );
        int warningY = panelBottom + 6;
        for (IReorderingProcessor warningLine : warningLines) {
            this.font.draw(matrixStack, warningLine, 20, warningY, 0xE6C77A);
            warningY += 10;
        }

        if (this.status != null) {
            drawCenteredString(matrixStack, this.font, this.status, center, this.height - 55, 0x8FE6A0);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void drawLabel(MatrixStack matrixStack, String key, int x, int y) {
        drawString(matrixStack, this.font, new TranslationTextComponent(key), x, y, 0x8EA0C4);
    }

    private void drawPanel(MatrixStack matrixStack, int x, int y, int width, int height, int border, int fillColor) {
        fill(matrixStack, x, y, x + width, y + height, border);
        fill(matrixStack, x + 2, y + 2, x + width - 2, y + height - 2, fillColor);
    }

    private void saveValues() {
        try {
            int size = clamp(Integer.parseInt(this.pocketSize.getValue()), 16, 256);
            if (size % 2 != 0) size--;

            int spacing = clamp(Integer.parseInt(this.pocketSpacing.getValue()), 64, 10000);
            spacing = Math.max(spacing, size + 16);
            int floor = clamp(Integer.parseInt(this.floorY.getValue()), 16, 240);
            int radius = clamp(Integer.parseInt(this.islandRadius.getValue()), 4, Math.max(4, size / 2 - 2));
            int thickness = clamp(Integer.parseInt(this.islandThickness.getValue()), 1, 32);
            int border = clamp(Integer.parseInt(this.borderHeight.getValue()), 1, 16);
            int duration = clamp(Integer.parseInt(this.riftDuration.getValue()), 1, 600);
            int width = clamp(Integer.parseInt(this.riftWidth.getValue()), 1, 8);
            int height = clamp(Integer.parseInt(this.riftHeight.getValue()), 1, 16);
            double damage = clamp(Double.parseDouble(this.voidDamage.getValue().replace(',', '.')), 0.0D, 20.0D);

            PocketMapConfig.applyValues(size, spacing, floor, radius, thickness, border, duration, width, height, damage);
            PocketNetwork.sendToServer(new MapConfigPacket(
                    size, spacing, floor, radius, thickness, border, duration, width, height, damage
            ));
            fillFieldsFromConfig();
            this.status = new TranslationTextComponent("screen.pocketdimension.map_saved");
        } catch (NumberFormatException exception) {
            this.status = new TranslationTextComponent("screen.pocketdimension.map_invalid");
        }
    }

    private void resetValues() {
        PocketMapConfig.resetToDefaults();
        PocketNetwork.sendToServer(new MapConfigPacket(
                PocketMapConfig.pocketSize(),
                PocketMapConfig.pocketSpacing(),
                PocketMapConfig.floorY(),
                PocketMapConfig.islandRadius(),
                PocketMapConfig.islandThickness(),
                PocketMapConfig.borderHeight(),
                PocketMapConfig.RIFT_DURATION_SECONDS.get(),
                PocketMapConfig.riftWidth(),
                PocketMapConfig.riftHeight(),
                PocketMapConfig.VOID_SHEAR_DAMAGE.get()
        ));
        fillFieldsFromConfig();
        this.status = new TranslationTextComponent("screen.pocketdimension.map_defaults_applied");
    }

    private void fillFieldsFromConfig() {
        this.pocketSize.setValue(Integer.toString(PocketMapConfig.pocketSize()));
        this.pocketSpacing.setValue(Integer.toString(PocketMapConfig.pocketSpacing()));
        this.floorY.setValue(Integer.toString(PocketMapConfig.floorY()));
        this.islandRadius.setValue(Integer.toString(PocketMapConfig.islandRadius()));
        this.islandThickness.setValue(Integer.toString(PocketMapConfig.islandThickness()));
        this.borderHeight.setValue(Integer.toString(PocketMapConfig.borderHeight()));
        this.riftDuration.setValue(Integer.toString(PocketMapConfig.RIFT_DURATION_SECONDS.get()));
        this.riftWidth.setValue(Integer.toString(PocketMapConfig.riftWidth()));
        this.riftHeight.setValue(Integer.toString(PocketMapConfig.riftHeight()));
        this.voidDamage.setValue(Double.toString(PocketMapConfig.VOID_SHEAR_DAMAGE.get()));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void onClose() {
        closeEditor();
    }

    private void closeEditor() {
        if (this.editorMode) {
            PocketNetwork.sendToServer(new EditorModePacket(false));
        }
        this.minecraft.setScreen(this.parent);
    }
}
