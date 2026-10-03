package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.container.StabilizerMachineContainer;
import com.fosder.pocketdimension.setup.ModItems;
import com.fosder.pocketdimension.util.PocketClientConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class StabilizerMachineScreen extends ContainerScreen<StabilizerMachineContainer> {
    private static final int BG = 0xFF03050B;
    private static final int PANEL = 0xFF0A0F1E;
    private static final int PANEL_2 = 0xFF101827;
    private static final int PANEL_3 = 0xFF172033;
    private static final int BORDER = 0xFF1DEBFF;
    private static final int PURPLE = 0xFF6B4DFF;
    private static final int TEXT = 0xFFD8F6FF;
    private static final int MUTED = 0xFF8EA0C4;
    private static final int GOLD = 0xFFFFD166;

    private int selectedTab = 0;
    private int upgradeScroll = 0;
    private Button mapEditorButton;

    public StabilizerMachineScreen(StabilizerMachineContainer menu, PlayerInventory inventory, ITextComponent title) {
        super(menu, inventory, title);
        this.imageWidth = 292;
        this.imageHeight = 306;
        this.titleLabelX = 12;
        this.titleLabelY = 8;
        this.inventoryLabelX = 65;
        this.inventoryLabelY = 200;
    }

    @Override
    protected void init() {
        super.init();
        this.mapEditorButton = this.addButton(new Button(
                this.leftPos + 111,
                this.topPos + 154,
                130,
                20,
                editorButtonLabel(),
                button -> {
                    boolean active = !this.menu.isEditorMode();
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            StabilizerMachineContainer.MAP_EDITOR_BUTTON
                    );
                    if (active) {
                        this.minecraft.setScreen(new PocketMapEditorScreen(this, true));
                    }
                }
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.mapEditorButton != null) {
            this.mapEditorButton.setMessage(editorButtonLabel());
        }
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderTooltips(matrixStack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = this.leftPos;
        int y = this.topPos;
        for (int i = 0; i < 3; i++) {
            if (inside((int) mouseX, (int) mouseY, x + 10 + i * 91, y + 27, 86, 22)) {
                this.selectedTab = i;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (selectedTab == 1 && inside((int) mouseX, (int) mouseY, this.leftPos + 10, this.topPos + 55, 272, 126)) {
            this.upgradeScroll = Math.max(0, Math.min(42, this.upgradeScroll + (delta < 0 ? 14 : -14)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        fill(stack, x, y, x + imageWidth, y + imageHeight, 0xFF000000);
        fill(stack, x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, PANEL);
        fill(stack, x + 8, y + 21, x + imageWidth - 8, y + 24, BORDER);

        drawTabs(stack, x, y);
        drawSection(stack, x + 10, y + 55, 272, 126, 0xFF050813, 0xFF0B1222, tabColor());

        if (selectedTab == 0) {
            drawStatusTab(stack, x, y);
        } else if (selectedTab == 1) {
            drawUpgradeTab(stack, x, y);
        } else {
            drawStatsTab(stack, x, y);
        }

        drawInventorySlots(stack, x + 65, y + 211, 3);
        drawInventorySlots(stack, x + 65, y + 269, 1);
    }

    @Override
    protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
        this.font.draw(stack, this.title, this.titleLabelX, this.titleLabelY, 0x66F2FF);

        drawTabLabel(stack, new TranslationTextComponent("screen.pocketdimension.tab_status"), 26, 34, 0);
        drawTabLabel(stack, new TranslationTextComponent("screen.pocketdimension.tab_upgrades"), 112, 34, 1);
        drawTabLabel(stack, new TranslationTextComponent("screen.pocketdimension.tab_stats"), 192, 34, 2);

        if (selectedTab == 0) {
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.core"), 22, 65, 0xAEEBFF);
            this.font.draw(stack, new TranslationTextComponent(this.menu.isLinked()
                    ? "screen.pocketdimension.linked"
                    : "screen.pocketdimension.unlinked"), 19, 145, this.menu.isLinked() ? 0x91FFB2 : 0xFF7A7A);
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.dimension_status"), 111, 65, 0xAEEBFF);
            this.font.draw(stack, stateName(this.menu.getStateCode()), 111, 78, stateColor(this.menu.getStateCode()));
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.energy"), 111, 108, MUTED);
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.energy_value", this.menu.getEnergy(), this.menu.getCapacity()), 190, 108, TEXT);
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.stability"), 111, 139, MUTED);
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.percent", this.menu.getStability()), 220, 139, TEXT);
        } else if (selectedTab == 1) {
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.installed_upgrades"), 18, 64, GOLD);
            int sy = this.upgradeScroll;
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.level", this.menu.getSizeLevel()), 48, 111 - sy, 0xE7E7FF);
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.level", this.menu.getSecurityLevel()), 132, 111 - sy, 0xE7E7FF);
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.level", this.menu.getEnergyLevel()), 216, 111 - sy, 0xE7E7FF);
            this.font.draw(stack, new TranslationTextComponent(this.menu.isLinked()
                    ? "screen.pocketdimension.ok"
                    : "screen.pocketdimension.not_ok"), 48, 158 - sy, 0xE7E7FF);
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.auto_fuel"), 102, 158 - sy, 0xB8C7FF);
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.scroll"), 244, 64, 0x8EA0C4);
        } else {
            this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.statistics"), 18, 64, 0xAEEBFF);
            if (PocketClientConfig.DETAILED_MACHINE_INFO.get()) {
                this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.active_time", this.menu.getActiveMinutes()), 18, 84, TEXT);
                this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.base_drain", this.menu.getDrainPerCycle()), 18, 104, 0xFFB8B8);
                this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.next_drain", this.menu.getSecondsUntilNextDrain()), 18, 124, 0x8FEAFF);
                this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.numeric_state", this.menu.getStateCode()), 18, 144, MUTED);
            } else {
                this.font.draw(stack, new TranslationTextComponent("screen.pocketdimension.basic_stats"), 18, 92, TEXT);
            }
        }

        this.font.draw(stack, this.inventory.getDisplayName(), this.inventoryLabelX, this.inventoryLabelY, 0xB8B8C8);
    }

    private void drawStatusTab(MatrixStack stack, int x, int y) {
        drawSection(stack, x + 18, y + 72, 76, 86, 0xFF050813, PANEL_2, BORDER);
        drawReactor(stack, x + 37, y + 99);
        drawBar(stack, x + 111, y + 95, 154, 12, this.menu.getEnergy(), this.menu.getCapacity(), 0xFF122A33, 0xFF2DEBFF);
        drawBar(stack, x + 111, y + 126, 154, 12, this.menu.getStability(), 100, 0xFF182718, stateColor(this.menu.getStateCode()));
    }

    private void drawUpgradeTab(MatrixStack stack, int x, int y) {
        int sy = this.upgradeScroll;
        drawModule(stack, x + 18, y + 82 - sy, new ItemStack(ModItems.SIZE_UPGRADE.get()), new TranslationTextComponent("screen.pocketdimension.module_size"));
        drawModule(stack, x + 102, y + 82 - sy, new ItemStack(ModItems.SECURITY_UPGRADE.get()), new TranslationTextComponent("screen.pocketdimension.module_security"));
        drawModule(stack, x + 186, y + 82 - sy, new ItemStack(ModItems.ENERGY_UPGRADE.get()), new TranslationTextComponent("screen.pocketdimension.module_energy"));
        drawModule(stack, x + 18, y + 129 - sy, new ItemStack(ModItems.DIMENSIONAL_KEY.get()), new TranslationTextComponent("screen.pocketdimension.module_key"));
        drawAutoFuelSlot(stack, x + 108, y + 138 - sy);
        drawScrollBar(stack, x + 270, y + 62);
    }

    private void drawStatsTab(MatrixStack stack, int x, int y) {
        drawBar(stack, x + 18, y + 73, 230, 12, this.menu.getEnergy(), this.menu.getCapacity(), 0xFF122A33, 0xFF2DEBFF);
        drawBar(stack, x + 18, y + 93, 230, 12, this.menu.getStability(), 100, 0xFF182718, stateColor(this.menu.getStateCode()));
        fill(stack, x + 18, y + 115, x + 248, y + 117, PURPLE);
    }

    private void drawTabs(MatrixStack stack, int x, int y) {
        for (int i = 0; i < 3; i++) {
            int tx = x + 10 + i * 91;
            int color = selectedTab == i ? 0xFF14243A : 0xFF070B14;
            int accent = selectedTab == i ? tabColor() : 0xFF2B3657;
            fill(stack, tx, y + 27, tx + 86, y + 49, 0xFF000000);
            fill(stack, tx + 1, y + 28, tx + 85, y + 48, color);
            fill(stack, tx + 2, y + 29, tx + 84, y + 31, accent);
        }
    }

    private void drawTabLabel(MatrixStack stack, ITextComponent label, int x, int y, int tab) {
        this.font.draw(stack, label, x, y, selectedTab == tab ? 0xFFFFFFFF : 0xFF8EA0C4);
    }

    private int tabColor() {
        return selectedTab == 1 ? GOLD : selectedTab == 2 ? PURPLE : BORDER;
    }

    private void drawSection(MatrixStack stack, int x, int y, int w, int h, int border, int inside, int accent) {
        fill(stack, x, y, x + w, y + h, border);
        fill(stack, x + 2, y + 2, x + w - 2, y + h - 2, inside);
        fill(stack, x + 3, y + 3, x + w - 3, y + 5, accent);
    }

    private void drawReactor(MatrixStack stack, int x, int y) {
        fill(stack, x - 8, y - 8, x + 46, y + 46, 0xFF050710);
        fill(stack, x - 5, y - 5, x + 43, y + 43, 0xFF101827);
        fill(stack, x + 1, y + 1, x + 37, y + 37, 0xFF172A44);
        int core = this.menu.isLinked() ? 0xFF2DEBFF : 0xFF4B5563;
        fill(stack, x + 11, y + 11, x + 27, y + 27, core);
        fill(stack, x + 15, y + 15, x + 23, y + 23, this.menu.getEnergy() > 0 ? 0xFFFFFFFF : 0xFF111827);
    }

    private void drawBar(MatrixStack stack, int x, int y, int width, int height, int value, int max, int bg, int fg) {
        int safeMax = Math.max(1, max);
        int filled = Math.max(0, Math.min(width, (value * width) / safeMax));
        fill(stack, x - 1, y - 1, x + width + 1, y + height + 1, 0xFF000000);
        fill(stack, x, y, x + width, y + height, bg);
        fill(stack, x, y, x + filled, y + height, fg);
        if (filled > 0) fill(stack, x, y, x + filled, y + 1, 0x99FFFFFF);
    }

    private void drawModule(MatrixStack stack, int x, int y, ItemStack icon, ITextComponent label) {
        fill(stack, x, y, x + 76, y + 42, 0xFF060B16);
        fill(stack, x + 1, y + 1, x + 75, y + 41, PANEL_3);
        this.itemRenderer.renderAndDecorateItem(icon, x + 6, y + 8);
        this.font.draw(stack, label, x + 27, y + 10, 0xD7E9FF);
    }

    private void drawAutoFuelSlot(MatrixStack stack, int x, int y) {
        fill(stack, x - 2, y - 2, x + 20, y + 20, 0xFF000000);
        fill(stack, x - 1, y - 1, x + 19, y + 19, 0xFF102036);
        fill(stack, x, y, x + 18, y + 18, 0xFF20263A);
        fill(stack, x + 2, y + 2, x + 16, y + 16, 0xFF05070D);
    }

    private void drawScrollBar(MatrixStack stack, int x, int y) {
        fill(stack, x, y, x + 4, y + 111, 0xFF05070D);
        int knob = y + 2 + (this.upgradeScroll * 81) / 42;
        fill(stack, x + 1, knob, x + 3, knob + 26, GOLD);
    }

    private void drawInventorySlots(MatrixStack stack, int x, int y, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + col * 18;
                int sy = y + row * 18;
                fill(stack, sx, sy, sx + 18, sy + 18, 0xFF05070D);
                fill(stack, sx + 1, sy + 1, sx + 17, sy + 17, 0xFF20263A);
            }
        }
    }

    private void renderTooltips(MatrixStack stack, int mouseX, int mouseY) {
        if (!PocketClientConfig.GUI_TOOLTIPS.get()) {
            return;
        }

        int x = this.leftPos;
        int y = this.topPos;

        if (inside(mouseX, mouseY, x + 10, y + 27, 86, 22)) {
            this.renderTooltip(stack, new TranslationTextComponent("screen.pocketdimension.tooltip_status"), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, x + 101, y + 27, 86, 22)) {
            this.renderTooltip(stack, new TranslationTextComponent("screen.pocketdimension.tooltip_upgrades"), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, x + 192, y + 27, 86, 22)) {
            this.renderTooltip(stack, new TranslationTextComponent("screen.pocketdimension.tooltip_stats"), mouseX, mouseY);
        }
    }

    private ITextComponent editorButtonLabel() {
        return new TranslationTextComponent(
                "button.pocketdimension.map_editor",
                new TranslationTextComponent(this.menu.isEditorMode()
                        ? "value.pocketdimension.active"
                        : "value.pocketdimension.inactive")
        );
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private int stateColor(int code) {
        switch (code) {
            case 4: return 0xFF67FF8F;
            case 3: return 0xFFFFD166;
            case 2: return 0xFFFF8C42;
            case 1: return 0xFFFF4D6D;
            case -1: return 0xFF6B7280;
            default: return 0xFF9CA3AF;
        }
    }

    private ITextComponent stateName(int code) {
        switch (code) {
            case 4: return new TranslationTextComponent("screen.pocketdimension.state.stable");
            case 3: return new TranslationTextComponent("screen.pocketdimension.state.degrading");
            case 2: return new TranslationTextComponent("screen.pocketdimension.state.critical");
            case 1: return new TranslationTextComponent("screen.pocketdimension.state.emergency");
            case -1: return new TranslationTextComponent("screen.pocketdimension.state.off");
            default: return new TranslationTextComponent("screen.pocketdimension.state.unlinked");
        }
    }
}
