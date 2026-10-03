package com.fosder.pocketdimension.client;

import com.fosder.pocketdimension.util.PocketClientConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Créditos de traducción local o descargados desde una distribución Crowdin.
 *
 * Crowdin es opcional: si no está configurado o falla, se mantienen los
 * créditos incluidos en los archivos de idioma y la última caché válida.
 */
public class PocketCommunityTranslationsScreen extends Screen {
    private static final int ROW_HEIGHT = 48;
    private static final List<TranslationCredit> LOCAL_CREDITS = Arrays.asList(
            TranslationCredit.local("ES", "credits.pocketdimension.language.es_es", "credits.pocketdimension.author.es_es"),
            TranslationCredit.local("EN", "credits.pocketdimension.language.en_us", "credits.pocketdimension.author.en_us"),
            TranslationCredit.local("FR", "credits.pocketdimension.language.fr_fr", "credits.pocketdimension.author.fr_fr"),
            TranslationCredit.local("DE", "credits.pocketdimension.language.de_de", "credits.pocketdimension.author.de_de"),
            TranslationCredit.local("PT", "credits.pocketdimension.language.pt_br", "credits.pocketdimension.author.pt_br")
    );

    private final Screen parent;
    private List<TranslationCredit> credits = new ArrayList<>(LOCAL_CREDITS);
    private ITextComponent apiStatus = new TranslationTextComponent(
            "screen.pocketdimension.community_translation.api_local"
    );
    private int scrollOffset;
    private boolean apiRequestStarted;

    public PocketCommunityTranslationsScreen(Screen parent) {
        super(new TranslationTextComponent("screen.pocketdimension.community_translation_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.scrollOffset = Math.min(this.scrollOffset, maxScroll());
        if (!this.apiRequestStarted) {
            this.apiRequestStarted = true;
            loadCredits();
        }
        this.addButton(new Button(
                this.width / 2 - 50,
                this.height - 34,
                100,
                20,
                new TranslationTextComponent("button.pocketdimension.back"),
                button -> this.minecraft.setScreen(this.parent)
        ));
    }

    private void loadCredits() {
        List<PocketTranslationApiClient.RemoteCredit> cached = PocketTranslationApiClient.loadCached();
        final boolean hasCached = !cached.isEmpty();
        if (hasCached) {
            this.credits = fromRemote(cached);
            this.apiStatus = new TranslationTextComponent(
                    "screen.pocketdimension.community_translation.api_cached"
            );
        }

        String endpoint = PocketClientConfig.COMMUNITY_TRANSLATION_API_URL.get().trim();
        if (endpoint.isEmpty()) {
            if (!hasCached) {
                this.apiStatus = new TranslationTextComponent(
                        "screen.pocketdimension.community_translation.api_local"
                );
            }
            return;
        }

        this.apiStatus = new TranslationTextComponent(
                "screen.pocketdimension.community_translation.api_loading"
        );
        PocketTranslationApiClient.fetch(
                endpoint,
                PocketClientConfig.COMMUNITY_TRANSLATION_API_TIMEOUT_MS.get(),
                new PocketTranslationApiClient.Callback() {
                    @Override
                    public void onSuccess(List<PocketTranslationApiClient.RemoteCredit> remoteCredits) {
                        PocketCommunityTranslationsScreen.this.minecraft.execute(() -> {
                            PocketCommunityTranslationsScreen.this.credits = fromRemote(remoteCredits);
                            PocketCommunityTranslationsScreen.this.scrollOffset = Math.min(
                                    PocketCommunityTranslationsScreen.this.scrollOffset,
                                    PocketCommunityTranslationsScreen.this.maxScroll()
                            );
                            PocketCommunityTranslationsScreen.this.apiStatus = new TranslationTextComponent(
                                    "screen.pocketdimension.community_translation.api_online"
                            );
                        });
                    }

                    @Override
                    public void onFailure(String reason) {
                        PocketCommunityTranslationsScreen.this.minecraft.execute(() -> {
                            PocketCommunityTranslationsScreen.this.apiStatus = new TranslationTextComponent(
                                    hasCached
                                            ? "screen.pocketdimension.community_translation.api_cached"
                                            : "screen.pocketdimension.community_translation.api_offline"
                            );
                        });
                    }
                }
        );
    }

    private static List<TranslationCredit> fromRemote(List<PocketTranslationApiClient.RemoteCredit> remoteCredits) {
        List<TranslationCredit> result = new ArrayList<>();
        for (PocketTranslationApiClient.RemoteCredit credit : remoteCredits) {
            result.add(TranslationCredit.remote(credit));
        }
        return result.isEmpty() ? new ArrayList<>(LOCAL_CREDITS) : result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int panelLeft = Math.max(20, this.width / 2 - 250);
        int panelRight = Math.min(this.width - 20, this.width / 2 + 250);
        int contentTop = 84;
        int contentBottom = this.height - 76;
        if (mouseX >= panelLeft && mouseX <= panelRight
                && mouseY >= contentTop && mouseY <= contentBottom) {
            int amount = delta < 0 ? 24 : -24;
            this.scrollOffset = Math.max(0, Math.min(maxScroll(), this.scrollOffset + amount));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        int center = this.width / 2;
        int panelX = Math.max(20, center - 250);
        int panelWidth = Math.min(500, this.width - panelX * 2);
        int panelTop = 50;
        int panelBottom = this.height - 56;
        int contentTop = 84;
        int contentBottom = this.height - 76;

        drawCenteredString(matrixStack, this.font, this.title, center, 16, 0xFFFFFF);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.community_translation_description"),
                center, 31, 0xB8C6D9);
        drawCenteredString(matrixStack, this.font, this.apiStatus, center, 43, 0xD8C58A);

        fill(matrixStack, panelX, panelTop, panelX + panelWidth, panelBottom, 0xFFC49A4D);
        fill(matrixStack, panelX + 2, panelTop + 2, panelX + panelWidth - 2, panelBottom - 2, 0xFF172018);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.community_translation_section"),
                center, panelTop + 12, 0xFFFFD166);

        int y = contentTop - this.scrollOffset;
        for (TranslationCredit credit : this.credits) {
            if (y >= contentTop && y + 36 <= contentBottom) {
                fill(matrixStack, panelX + 18, y, panelX + panelWidth - 28, y + 36, 0xFF263526);
                fill(matrixStack, panelX + 18, y, panelX + 21, y + 36, 0xFFB87333);
                fill(matrixStack, panelX + 27, y + 7, panelX + 48, y + 28, 0xFF0B120B);
                drawCenteredString(matrixStack, this.font, new StringTextComponent(credit.code), panelX + 37, y + 13, 0xFFFFD166);
                this.font.draw(matrixStack,
                        new TranslationTextComponent("screen.pocketdimension.community_translation_credit",
                                credit.language,
                                credit.author),
                        panelX + 58, y + 7, 0xD8F6FF);
                this.font.draw(matrixStack, credit.status, panelX + 58, y + 21, 0x8FE6A0);
            }
            y += ROW_HEIGHT;
        }

        y += 4;
        List<IReorderingProcessor> helpLines = this.font.split(
                new TranslationTextComponent("screen.pocketdimension.community_translation_help"),
                panelWidth - 58
        );
        for (IReorderingProcessor helpLine : helpLines) {
            if (y >= contentTop && y + 10 <= contentBottom) {
                this.font.draw(matrixStack, helpLine, panelX + 30, y, 0xE6C77A);
            }
            y += 11;
        }

        drawScrollBar(matrixStack, panelX + panelWidth - 14, contentTop, contentBottom);
        drawCenteredString(matrixStack, this.font,
                new TranslationTextComponent("screen.pocketdimension.community_translation_scroll"),
                center, this.height - 50, 0xD8C58A);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void drawScrollBar(MatrixStack matrixStack, int x, int top, int bottom) {
        int height = Math.max(1, bottom - top);
        int contentHeight = this.credits.size() * ROW_HEIGHT + 90;
        int max = maxScroll();
        fill(matrixStack, x, top, x + 4, bottom, 0xFF0B120B);
        int knobHeight = Math.max(24, height * height / Math.max(height, contentHeight));
        int knobTravel = Math.max(0, height - knobHeight);
        int knobY = top + (max == 0 ? 0 : (this.scrollOffset * knobTravel) / max);
        fill(matrixStack, x + 1, knobY, x + 3, knobY + knobHeight, 0xFFFFD166);
    }

    private int maxScroll() {
        int contentHeight = this.credits.size() * ROW_HEIGHT + 90;
        int visibleHeight = Math.max(1, this.height - 160);
        return Math.max(0, contentHeight - visibleHeight);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private static final class TranslationCredit {
        private final String code;
        private final ITextComponent language;
        private final ITextComponent author;
        private final ITextComponent status;

        private TranslationCredit(String code, ITextComponent language, ITextComponent author, ITextComponent status) {
            this.code = code;
            this.language = language;
            this.author = author;
            this.status = status;
        }

        private static TranslationCredit local(String code, String languageKey, String authorKey) {
            return new TranslationCredit(
                    code,
                    new TranslationTextComponent(languageKey),
                    new TranslationTextComponent(authorKey),
                    new TranslationTextComponent("screen.pocketdimension.community_translation_status")
            );
        }

        private static TranslationCredit remote(PocketTranslationApiClient.RemoteCredit credit) {
            return new TranslationCredit(
                    credit.getCode(),
                    new StringTextComponent(credit.getLanguage()),
                    new StringTextComponent(credit.getAuthor()),
                    new StringTextComponent(credit.getStatus())
            );
        }
    }
}
