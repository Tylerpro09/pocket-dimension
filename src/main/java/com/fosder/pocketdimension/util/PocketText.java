package com.fosder.pocketdimension.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public final class PocketText {
    private PocketText() {}

    public static ITextComponent translate(String key, Object... args) {
        return new TranslationTextComponent(key, args);
    }

    public static void send(PlayerEntity player, String key, Object... args) {
        IFormattableTextComponent prefix = new TranslationTextComponent("text.pocketdimension.prefix")
                .withStyle(TextFormatting.DARK_PURPLE);
        IFormattableTextComponent message = new TranslationTextComponent(key, args);
        player.sendMessage(prefix.append(new TranslationTextComponent("text.pocketdimension.separator"))
                .append(message), player.getUUID());
    }
}
