package com.fosder.pocketdimension.util;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configuración editable del mapa de la dimensión bolsillo.
 *
 * Es una configuración COMMON para que el tamaño y las reglas del mapa sean
 * propiedad del mundo, no solo de la interfaz del jugador.
 */
public final class PocketMapConfig {
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final ForgeConfigSpec.IntValue POCKET_SIZE;
    public static final ForgeConfigSpec.IntValue POCKET_SPACING;
    public static final ForgeConfigSpec.IntValue FLOOR_Y;
    public static final ForgeConfigSpec.IntValue ISLAND_RADIUS;
    public static final ForgeConfigSpec.IntValue ISLAND_THICKNESS;
    public static final ForgeConfigSpec.IntValue BORDER_HEIGHT;
    public static final ForgeConfigSpec.IntValue RIFT_DURATION_SECONDS;
    public static final ForgeConfigSpec.IntValue RIFT_WIDTH;
    public static final ForgeConfigSpec.IntValue RIFT_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue VOID_SHEAR_DAMAGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Editable world map layout for Pocket Dimension.").push("map");
        POCKET_SIZE = builder
                .comment("Playable square size. Even values from 16 to 256 are supported.")
                .defineInRange("pocketSize", 64, 16, 256);
        POCKET_SPACING = builder
                .comment("Distance between player pockets in the shared void world.")
                .defineInRange("pocketSpacing", 256, 64, 10000);
        FLOOR_Y = builder
                .comment("Vertical center and floor height of every pocket.")
                .defineInRange("floorY", 64, 16, 240);
        ISLAND_RADIUS = builder
                .comment("Radius of the generated circular island.")
                .defineInRange("islandRadius", 30, 4, 124);
        ISLAND_THICKNESS = builder
                .comment("Depth of the generated island below its surface.")
                .defineInRange("islandThickness", 6, 1, 32);
        BORDER_HEIGHT = builder
                .comment("Height of the visible pocket border effect.")
                .defineInRange("borderHeight", 3, 1, 16);
        builder.pop();

        builder.comment("Editable temporary-rift rules for the map.").push("rifts");
        RIFT_DURATION_SECONDS = builder
                .comment("How many seconds a key-created rift remains open.")
                .defineInRange("durationSeconds", 18, 1, 600);
        RIFT_WIDTH = builder
                .comment("Width of a temporary rift.")
                .defineInRange("width", 2, 1, 8);
        RIFT_HEIGHT = builder
                .comment("Height of a temporary rift.")
                .defineInRange("height", 3, 1, 16);
        builder.pop();

        builder.comment("Editable void damage rule.").push("safety");
        VOID_SHEAR_DAMAGE = builder
                .comment("Damage applied before automatic rescue at the pocket border.")
                .defineInRange("voidShearDamage", 3.0D, 0.0D, 20.0D);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private PocketMapConfig() {}

    public static int pocketSize() {
        int value = POCKET_SIZE.get();
        return value % 2 == 0 ? value : value - 1;
    }

    public static int halfSize() {
        return pocketSize() / 2;
    }

    public static int pocketSpacing() {
        return Math.max(POCKET_SPACING.get(), pocketSize() + 16);
    }

    public static int floorY() {
        return FLOOR_Y.get();
    }

    public static int safeY() {
        return floorY() + 1;
    }

    public static int islandRadius() {
        return Math.min(ISLAND_RADIUS.get(), Math.max(4, halfSize() - 2));
    }

    public static int islandThickness() {
        return ISLAND_THICKNESS.get();
    }

    public static int borderHeight() {
        return BORDER_HEIGHT.get();
    }

    public static int riftDurationTicks() {
        return RIFT_DURATION_SECONDS.get() * 20;
    }

    public static int riftWidth() {
        return RIFT_WIDTH.get();
    }

    public static int riftHeight() {
        return RIFT_HEIGHT.get();
    }

    public static float voidShearDamage() {
        return VOID_SHEAR_DAMAGE.get().floatValue();
    }

    public static void resetToDefaults() {
        applyValues(64, 256, 64, 30, 6, 3, 18, 2, 3, 3.0D);
    }

    public static void applyValues(int pocketSize, int pocketSpacing, int floorY, int islandRadius,
                                   int islandThickness, int borderHeight, int riftDurationSeconds,
                                   int riftWidth, int riftHeight, double voidDamage) {
        int safeSize = Math.max(16, Math.min(256, pocketSize));
        if (safeSize % 2 != 0) safeSize--;
        int safeSpacing = Math.max(64, Math.min(10000, pocketSpacing));
        safeSpacing = Math.max(safeSpacing, safeSize + 16);
        POCKET_SIZE.set(safeSize);
        POCKET_SPACING.set(safeSpacing);
        FLOOR_Y.set(Math.max(16, Math.min(240, floorY)));
        ISLAND_RADIUS.set(Math.max(4, Math.min(124, islandRadius)));
        ISLAND_THICKNESS.set(Math.max(1, Math.min(32, islandThickness)));
        BORDER_HEIGHT.set(Math.max(1, Math.min(16, borderHeight)));
        RIFT_DURATION_SECONDS.set(Math.max(1, Math.min(600, riftDurationSeconds)));
        RIFT_WIDTH.set(Math.max(1, Math.min(8, riftWidth)));
        RIFT_HEIGHT.set(Math.max(1, Math.min(16, riftHeight)));
        VOID_SHEAR_DAMAGE.set(Math.max(0.0D, Math.min(20.0D, voidDamage)));
        COMMON_SPEC.save();
    }
}
