package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.PocketDimensionMod;
import com.fosder.pocketdimension.container.DimensionalWorkbenchContainer;
import com.fosder.pocketdimension.setup.ModItems;
import com.fosder.pocketdimension.util.PocketClientConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * GUI propio de la Mesa Dimensional.
 * Tiene un libro de recetas integrado estilo vanilla, limitado solo a recetas dimensionales.
 */
public class DimensionalWorkbenchScreen extends ContainerScreen<DimensionalWorkbenchContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            PocketDimensionMod.MOD_ID,
            "textures/gui/dimensional_workbench.png"
    );

    /** Cuántas recetas dimensionales puede mostrar el libro por página. */
    private static final int MAX_RECIPES_PER_PAGE = 8;

    private static final int BOOK_W = 147;
    private static final int BOOK_H = 166;
    private static final int BOOK_SEARCH_H = 14;
    private static final int RECIPE_ICON_SIZE = 25;
    private static final int RECIPE_ICON_GAP = 4;

    private boolean recipeBookOpen = true;
    private int recipePage = 0;
    private int selectedRecipe = 0;

    private static final RecipeInfo[] RECIPES = new RecipeInfo[] {
            new RecipeInfo(
                    new ItemStack(ModItems.VOID_SHARD.get(), 4),
                    "item.pocketdimension.void_shard",
                    new ItemStack[] {
                            new ItemStack(Items.OBSIDIAN), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.OBSIDIAN),
                            ItemStack.EMPTY, new ItemStack(Items.QUARTZ), ItemStack.EMPTY,
                            new ItemStack(Items.OBSIDIAN), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.OBSIDIAN)
                    }
            ),
            new RecipeInfo(
                    new ItemStack(ModItems.STABILIZER_BATTERY.get(), 2),
                    "item.pocketdimension.stabilizer_battery",
                    new ItemStack[] {
                            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                            ItemStack.EMPTY, new ItemStack(ModItems.STABILIZER_BATTERY_ORE_ITEM.get()), ItemStack.EMPTY,
                            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
                    }
            ),
            new RecipeInfo(
                    new ItemStack(ModItems.STABILIZER_BATTERY.get()),
                    "item.pocketdimension.stabilizer_battery",
                    new ItemStack[] {
                            new ItemStack(ModItems.VOID_SHARD.get()), new ItemStack(Items.REDSTONE), new ItemStack(ModItems.VOID_SHARD.get()),
                            new ItemStack(Items.REDSTONE), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.REDSTONE),
                            new ItemStack(ModItems.VOID_SHARD.get()), new ItemStack(Items.REDSTONE), new ItemStack(ModItems.VOID_SHARD.get())
                    }
            ),
            new RecipeInfo(
                    new ItemStack(ModItems.POCKET_CORE.get()),
                    "item.pocketdimension.pocket_core",
                    new ItemStack[] {
                            new ItemStack(Items.DIAMOND), new ItemStack(ModItems.VOID_SHARD.get()), new ItemStack(Items.DIAMOND),
                            new ItemStack(ModItems.VOID_SHARD.get()), new ItemStack(Items.NETHER_STAR), new ItemStack(ModItems.VOID_SHARD.get()),
                            new ItemStack(Items.DIAMOND), new ItemStack(ModItems.VOID_SHARD.get()), new ItemStack(Items.DIAMOND)
                    }
            ),
            new RecipeInfo(
                    new ItemStack(ModItems.DIMENSIONAL_KEY.get()),
                    "item.pocketdimension.dimensional_key",
                    new ItemStack[] {
                            new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.GOLD_INGOT),
                            ItemStack.EMPTY, new ItemStack(ModItems.POCKET_CORE.get()), ItemStack.EMPTY,
                            ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY
                    }
            ),
            new RecipeInfo(
                    new ItemStack(ModItems.VOID_STABILIZER.get()),
                    "item.pocketdimension.void_stabilizer",
                    new ItemStack[] {
                            new ItemStack(Items.OBSIDIAN), new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.OBSIDIAN),
                            new ItemStack(Items.BLAZE_ROD), new ItemStack(ModItems.POCKET_CORE.get()), new ItemStack(Items.BLAZE_ROD),
                            new ItemStack(Items.OBSIDIAN), new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.OBSIDIAN)
                    }
            ),

            new RecipeInfo(
                    new ItemStack(ModItems.STABILIZER_MACHINE_ITEM.get()),
                    "block.pocketdimension.stabilizer_machine",
                    new ItemStack[] {
                            new ItemStack(Items.OBSIDIAN), new ItemStack(ModItems.VOID_STABILIZER.get()), new ItemStack(Items.OBSIDIAN),
                            new ItemStack(Items.REDSTONE_BLOCK), new ItemStack(ModItems.POCKET_CORE.get()), new ItemStack(Items.REDSTONE_BLOCK),
                            new ItemStack(Items.OBSIDIAN), new ItemStack(Items.SEA_LANTERN), new ItemStack(Items.OBSIDIAN)
                    }
            ),
            new RecipeInfo(
                    new ItemStack(ModItems.SIZE_UPGRADE.get()),
                    "item.pocketdimension.size_upgrade",
                    new ItemStack[] {
                            new ItemStack(Items.ENDER_EYE), new ItemStack(Items.DIAMOND), new ItemStack(Items.ENDER_EYE),
                            new ItemStack(Items.DIAMOND), new ItemStack(ModItems.POCKET_CORE.get()), new ItemStack(Items.DIAMOND),
                            new ItemStack(Items.ENDER_EYE), new ItemStack(Items.DIAMOND), new ItemStack(Items.ENDER_EYE)
                    }
            ),
            new RecipeInfo(
                    new ItemStack(ModItems.SECURITY_UPGRADE.get()),
                    "item.pocketdimension.security_upgrade",
                    new ItemStack[] {
                            new ItemStack(Items.OBSIDIAN), new ItemStack(Items.NETHERITE_INGOT), new ItemStack(Items.OBSIDIAN),
                            new ItemStack(Items.NETHERITE_INGOT), new ItemStack(ModItems.POCKET_CORE.get()), new ItemStack(Items.NETHERITE_INGOT),
                            new ItemStack(Items.OBSIDIAN), new ItemStack(Items.REDSTONE_BLOCK), new ItemStack(Items.OBSIDIAN)
                    }
            ),
            new RecipeInfo(
                    new ItemStack(ModItems.DIMENSIONAL_PORTAL_ITEM.get(), 6),
                    "block.pocketdimension.dimensional_portal",
                    new ItemStack[] {
                            new ItemStack(ModItems.VOID_SHARD.get()), new ItemStack(Items.ENDER_EYE), new ItemStack(ModItems.VOID_SHARD.get()),
                            new ItemStack(Items.ENDER_EYE), new ItemStack(ModItems.POCKET_CORE.get()), new ItemStack(Items.ENDER_EYE),
                            new ItemStack(ModItems.VOID_SHARD.get()), new ItemStack(Items.ENDER_EYE), new ItemStack(ModItems.VOID_SHARD.get())
                    }
            ),
            new RecipeInfo(
                    new ItemStack(ModItems.HACKER_KEY.get()),
                    "item.pocketdimension.hacker_key",
                    new ItemStack[] {
                            new ItemStack(Items.NETHERITE_INGOT), new ItemStack(ModItems.SECURITY_UPGRADE.get()), new ItemStack(Items.NETHERITE_INGOT),
                            new ItemStack(ModItems.SECURITY_UPGRADE.get()), new ItemStack(ModItems.DIMENSIONAL_KEY.get()), new ItemStack(ModItems.SECURITY_UPGRADE.get()),
                            new ItemStack(Items.NETHERITE_INGOT), new ItemStack(Items.BEACON), new ItemStack(Items.NETHERITE_INGOT)
                    }
            )
    };

    public DimensionalWorkbenchScreen(DimensionalWorkbenchContainer menu, PlayerInventory inventory, ITextComponent title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 180;
        this.titleLabelX = 28;
        this.titleLabelY = 7;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 76;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (this.recipeBookOpen) {
            this.renderVanillaLikeRecipeBook(matrixStack, mouseX, mouseY);
        }

        this.renderRecipeBookButton(matrixStack, mouseX, mouseY);
        if (PocketClientConfig.GUI_TOOLTIPS.get()) {
            this.renderRecipeTooltips(matrixStack, mouseX, mouseY);
            this.renderTooltip(matrixStack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(TEXTURE);
        this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.draw(matrixStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 0x7EEBFF);
        this.font.draw(matrixStack, new TranslationTextComponent("container.pocketdimension.private_recipes"), 28.0F, 68.0F, 0xC7D7FF);
        this.font.draw(matrixStack, this.inventory.getDisplayName(), (float) this.inventoryLabelX, (float) this.inventoryLabelY, 0xB8B8C8);
    }

    private void renderRecipeBookButton(MatrixStack matrixStack, int mouseX, int mouseY) {
        int x = this.leftPos + 5;
        int y = this.topPos + 5;
        int color = this.recipeBookOpen ? 0xFF6FBF5E : 0xFF4E8F47;
        fill(matrixStack, x, y, x + 18, y + 18, 0xFF1B1D22);
        fill(matrixStack, x + 1, y + 1, x + 17, y + 17, color);
        this.font.draw(matrixStack, new StringTextComponent("B"), x + 6, y + 5, 0xFFFFFFFF);
    }

    private void renderVanillaLikeRecipeBook(MatrixStack matrixStack, int mouseX, int mouseY) {
        int panelX = getBookX();
        int panelY = this.topPos + 7;

        // Panel estilo libro vanilla, pero oscuro/dimensional.
        fill(matrixStack, panelX, panelY, panelX + BOOK_W, panelY + BOOK_H, 0xFF252A35);
        fill(matrixStack, panelX + 3, panelY + 3, panelX + BOOK_W - 3, panelY + BOOK_H - 3, 0xFF111722);
        fill(matrixStack, panelX + 7, panelY + 7, panelX + BOOK_W - 7, panelY + 7 + BOOK_SEARCH_H, 0xFF0B0E14);
        fill(matrixStack, panelX + 8, panelY + 8, panelX + BOOK_W - 8, panelY + 6 + BOOK_SEARCH_H, 0xFF1C2633);

        this.font.draw(matrixStack, new TranslationTextComponent("container.pocketdimension.recipe_book_search"), panelX + 12, panelY + 10, 0xFFB8C6D9);

        int totalPages = getTotalPages();
        int start = this.recipePage * MAX_RECIPES_PER_PAGE;
        int end = Math.min(RECIPES.length, start + MAX_RECIPES_PER_PAGE);

        // Iconos de recetas: 2 columnas x 4 filas, limitado a recetas dimensionales.
        for (int i = start; i < end; i++) {
            int local = i - start;
            int col = local % 2;
            int row = local / 2;
            int iconX = panelX + 15 + col * (RECIPE_ICON_SIZE + RECIPE_ICON_GAP);
            int iconY = panelY + 32 + row * (RECIPE_ICON_SIZE + RECIPE_ICON_GAP);
            boolean selected = i == this.selectedRecipe;
            drawRecipeIcon(matrixStack, RECIPES[i].output, iconX, iconY, selected);
        }

        // Vista previa del crafteo seleccionado.
        RecipeInfo selected = RECIPES[this.selectedRecipe];
        int previewX = panelX + 82;
        int previewY = panelY + 34;
        this.font.draw(matrixStack, new TranslationTextComponent("container.pocketdimension.recipe_preview"), previewX - 2, previewY - 14, 0xFF7EEBFF);
        drawMiniCraftGrid(matrixStack, selected, previewX, previewY);

        int resultX = previewX + 24;
        int resultY = previewY + 67;
        fill(matrixStack, resultX - 3, resultY - 3, resultX + 19, resultY + 19, 0xFF2F3C4F);
        this.itemRenderer.renderAndDecorateItem(selected.output, resultX, resultY);
        this.itemRenderer.renderGuiItemDecorations(this.font, selected.output, resultX, resultY);

        int pageY = panelY + BOOK_H - 18;
        fill(matrixStack, panelX + 38, pageY - 2, panelX + 55, pageY + 13, 0xFF2A3342);
        fill(matrixStack, panelX + 93, pageY - 2, panelX + 110, pageY + 13, 0xFF2A3342);
        this.font.draw(matrixStack, new StringTextComponent("<"), panelX + 44, pageY + 1, 0xFFFFFFFF);
        this.font.draw(matrixStack, new StringTextComponent((this.recipePage + 1) + "/" + totalPages), panelX + 64, pageY + 1, 0xFFFFFFFF);
        this.font.draw(matrixStack, new StringTextComponent(">"), panelX + 99, pageY + 1, 0xFFFFFFFF);
    }

    private void drawRecipeIcon(MatrixStack matrixStack, ItemStack stack, int x, int y, boolean selected) {
        int border = selected ? 0xFFFFD36A : 0xFF3B4658;
        int bg = selected ? 0xFF344B64 : 0xFF1F2733;
        fill(matrixStack, x, y, x + RECIPE_ICON_SIZE, y + RECIPE_ICON_SIZE, border);
        fill(matrixStack, x + 1, y + 1, x + RECIPE_ICON_SIZE - 1, y + RECIPE_ICON_SIZE - 1, bg);
        this.itemRenderer.renderAndDecorateItem(stack, x + 4, y + 4);
        this.itemRenderer.renderGuiItemDecorations(this.font, stack, x + 4, y + 4);
    }

    private void drawMiniCraftGrid(MatrixStack matrixStack, RecipeInfo recipe, int x, int y) {
        for (int slot = 0; slot < 9; slot++) {
            int col = slot % 3;
            int row = slot / 3;
            int sx = x + col * 18;
            int sy = y + row * 18;
            fill(matrixStack, sx - 1, sy - 1, sx + 17, sy + 17, 0xFF384458);
            fill(matrixStack, sx, sy, sx + 16, sy + 16, 0xFF111721);
            ItemStack ingredient = recipe.ingredients[slot];
            if (!ingredient.isEmpty()) {
                this.itemRenderer.renderAndDecorateItem(ingredient, sx, sy);
            }
        }
    }

    private void renderRecipeTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (!this.recipeBookOpen) {
            return;
        }

        int panelX = getBookX();
        int panelY = this.topPos + 7;
        int start = this.recipePage * MAX_RECIPES_PER_PAGE;
        int end = Math.min(RECIPES.length, start + MAX_RECIPES_PER_PAGE);

        for (int i = start; i < end; i++) {
            int local = i - start;
            int col = local % 2;
            int row = local / 2;
            int iconX = panelX + 15 + col * (RECIPE_ICON_SIZE + RECIPE_ICON_GAP);
            int iconY = panelY + 32 + row * (RECIPE_ICON_SIZE + RECIPE_ICON_GAP);
            if (isInside(mouseX, mouseY, iconX, iconY, RECIPE_ICON_SIZE, RECIPE_ICON_SIZE)) {
                this.renderTooltip(matrixStack, new TranslationTextComponent(RECIPES[i].translationKey), mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int bookButtonX = this.leftPos + 5;
            int bookButtonY = this.topPos + 5;
            if (isInside(mouseX, mouseY, bookButtonX, bookButtonY, 18, 18)) {
                this.recipeBookOpen = !this.recipeBookOpen;
                return true;
            }

            if (this.recipeBookOpen) {
                int panelX = getBookX();
                int panelY = this.topPos + 7;
                int pageY = panelY + BOOK_H - 18;

                if (isInside(mouseX, mouseY, panelX + 38, pageY - 2, 17, 15)) {
                    this.recipePage = (this.recipePage - 1 + getTotalPages()) % getTotalPages();
                    clampSelectedToPage();
                    return true;
                }
                if (isInside(mouseX, mouseY, panelX + 93, pageY - 2, 17, 15)) {
                    this.recipePage = (this.recipePage + 1) % getTotalPages();
                    clampSelectedToPage();
                    return true;
                }

                int start = this.recipePage * MAX_RECIPES_PER_PAGE;
                int end = Math.min(RECIPES.length, start + MAX_RECIPES_PER_PAGE);
                for (int i = start; i < end; i++) {
                    int local = i - start;
                    int col = local % 2;
                    int row = local / 2;
                    int iconX = panelX + 15 + col * (RECIPE_ICON_SIZE + RECIPE_ICON_GAP);
                    int iconY = panelY + 32 + row * (RECIPE_ICON_SIZE + RECIPE_ICON_GAP);
                    if (isInside(mouseX, mouseY, iconX, iconY, RECIPE_ICON_SIZE, RECIPE_ICON_SIZE)) {
                        this.selectedRecipe = i;
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getBookX() {
        int leftBook = this.leftPos - BOOK_W;
        if (leftBook >= 4) {
            return leftBook;
        }
        return this.leftPos + this.imageWidth;
    }

    private int getTotalPages() {
        return Math.max(1, (RECIPES.length + MAX_RECIPES_PER_PAGE - 1) / MAX_RECIPES_PER_PAGE);
    }

    private void clampSelectedToPage() {
        int start = this.recipePage * MAX_RECIPES_PER_PAGE;
        int end = Math.min(RECIPES.length - 1, start + MAX_RECIPES_PER_PAGE - 1);
        if (this.selectedRecipe < start || this.selectedRecipe > end) {
            this.selectedRecipe = start;
        }
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static final class RecipeInfo {
        private final ItemStack output;
        private final String translationKey;
        private final ItemStack[] ingredients;

        private RecipeInfo(ItemStack output, String translationKey, ItemStack[] ingredients) {
            this.output = output;
            this.translationKey = translationKey;
            this.ingredients = ingredients;
        }
    }
}
