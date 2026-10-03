package com.fosder.pocketdimension.util;

/**
 * Configuración central del mod.
 *
 * Esta clase incluye los nombres nuevos y aliases de versiones anteriores para evitar
 * errores cuando Windows copia archivos encima de un src viejo sin borrar lo anterior.
 */
public final class PocketConfig {
    private PocketConfig() {}

    // ===============================
    // Bolsillos por jugador
    // ===============================

    /** Tamaño jugable del bolsillo. 64 = desde -32 hasta +32 alrededor del centro. */
    public static final int POCKET_SIZE = 64;
    public static final int HALF_SIZE = POCKET_SIZE / 2;

    /** Separación entre bolsillos dentro de la dimensión void estable. */
    public static final int POCKET_SPACING = 256;

    /** Altura del piso del bolsillo. Debajo de esto queda vacío. */
    public static final int FLOOR_Y = 64;

    /** Altura segura donde aparece el jugador. */
    public static final int SAFE_Y = FLOOR_Y + 1;

    /** Radio de la isla plana; deja un borde de vacío visible dentro del bolsillo. */
    public static final int ISLAND_RADIUS = HALF_SIZE - 2;

    /** Grosor de la isla plana, medido hacia abajo desde el césped. */
    public static final int ISLAND_THICKNESS = 6;

    /** Altura del borde visible rojo. */
    public static final int BORDER_HEIGHT = 3;

    // ===============================
    // Compatibilidad con clases antiguas
    // ===============================

    public static final int RADIUS = HALF_SIZE;
    public static final int DIAMETER = POCKET_SIZE;
    public static final int SPAWN_Y = SAFE_Y;
    public static final int SPAWN_PLATFORM_RADIUS = 3;
    public static final int MIN_BUILD_Y = 0;
    public static final int MAX_BUILD_Y = 255;
    public static final int MOB_CLEANUP_INTERVAL_TICKS = 20;

    public static final double BORDER_CENTER = 0.5D;
    public static final double BORDER_SIZE = POCKET_SIZE;
    public static final double BORDER_DAMAGE = 2.0D;

    // ===============================
    // Niebla cliente para ocultar el exterior
    // ===============================

    public static final float FOG_NEAR = 2.0F;
    public static final float FOG_FAR = 18.0F;
    public static final float FOG_DENSITY = 0.08F;
    public static final float FOG_RED = 0.015F;
    public static final float FOG_GREEN = 0.005F;
    public static final float FOG_BLUE = 0.030F;

    // ===============================
    // Coste fisico de uso dimensional
    // ===============================

    /** Energia que cuesta entrar o salir por teletransporte directo con la llave. */
    public static final int DIRECT_TELEPORT_ENERGY_COST = 35;

    /** Energia que cuesta cruzar una grieta fisica. Es menor porque la grieta ya esta abierta. */
    public static final int RIFT_TRAVEL_ENERGY_COST = 15;

    /** Energia que cuesta abrir una grieta temporal 2x3. */
    public static final int OPEN_RIFT_ENERGY_COST = 90;

    /** Cada tantos items transportados se suma 1 punto de energia al salto dimensional. */
    public static final int ITEM_MASS_PER_ENERGY = 16;

    /** Cada pieza de armadura equipada suma este coste de energia al salto dimensional. */
    public static final int ARMOR_MASS_ENERGY_COST = 4;

    /** Coste adicional por segundo mientras el modo editor mantiene el vuelo activo. */
    public static final int EDITOR_FLIGHT_ENERGY_PER_SECOND = 4;

    /** Estabilidad por debajo de este valor el bolsillo empieza a afectar al jugador. */
    public static final int LOW_STABILITY_PERCENT = 35;

    /** Estabilidad critica: hay fatiga, hambre y dano dimensional periodico. */
    public static final int CRITICAL_STABILITY_PERCENT = 15;

    /** Dano aplicado por entrar en el borde o caer al vacio antes del rescate. */
    public static final float VOID_SHEAR_DAMAGE = 3.0F;

    // ===============================
    // Fisica ambiental avanzada
    // ===============================

    public static final int ATMOSPHERE_CHECK_INTERVAL_TICKS = 40;
    public static final int ATMOSPHERE_SAMPLE_RADIUS = 7;
    public static final int MIN_OXYGEN_SCORE = 10;

    public static final int THERMAL_CHECK_INTERVAL_TICKS = 60;
    public static final int THERMAL_SAMPLE_RADIUS = 5;
    public static final int OVERHEAT_SCORE = 4;

    public static final int STRUCTURE_CHECK_INTERVAL_TICKS = 10;
    public static final int STRUCTURE_SAMPLE_RADIUS = 4;

    // ===============================
    // Retroalimentacion atmosferica al Overworld
    // ===============================

    public static final int HELL_GAS_TRIGGER_AIR_QUALITY = 12;
    public static final int HELL_GAS_TRIGGER_TREE_SCORE = 8;
    public static final int HELL_GAS_TERRAIN_INTERVAL_TICKS = 30;
    public static final int HELL_GAS_HEAT_INTERVAL_TICKS = 10;
    public static final int HELL_GAS_EFFECT_INTERVAL_TICKS = 60;
    public static final int HELL_GAS_WEATHER_INTERVAL_TICKS = 20;
    public static final int HELL_GAS_LIGHTNING_INTERVAL_TICKS = 80;
    public static final int HELL_GAS_DAY_PHASE_TICKS = 20 * 25;
    public static final int HELL_GAS_START_RADIUS = 18;
    public static final int HELL_GAS_MAX_RADIUS = 8000;
    public static final int HELL_GAS_EXPAND_INTERVAL_TICKS = 20 * 30;
    public static final int HELL_GAS_EXPAND_BLOCKS = 12;
    public static final int HELL_GAS_DEEP_SAFE_Y = 24;
    public static final int HELL_GAS_PARTIAL_SAFE_Y = 48;


    // ===============================
    // Grieta dimensional temporal
    // ===============================

    /** Duración de la grieta dimensional creada por la llave. 20 ticks = 1 segundo. */
    public static final int RIFT_DURATION_TICKS = 20 * 18;
    public static final int RIFT_WIDTH = 2;
    public static final int RIFT_HEIGHT = 3;

    public static final String TAG_RIFT_OPENED = "PocketRiftOpened";
    public static final String TAG_RIFT_EXPIRES = "PocketRiftExpires";

    // ===============================
    // Tags NBT
    // ===============================

    public static final String TAG_ROOT = "PocketDimension";
    public static final String TAG_RETURN_DIM = "ReturnDimension";
    public static final String TAG_RETURN_X = "ReturnX";
    public static final String TAG_RETURN_Y = "ReturnY";
    public static final String TAG_RETURN_Z = "ReturnZ";
    public static final String TAG_RETURN_YAW = "ReturnYaw";
    public static final String TAG_RETURN_PITCH = "ReturnPitch";
    public static final String TAG_KEY_OWNER = "Owner";
    public static final String TAG_KEY_OWNER_NAME = "OwnerName";

    // Valores del mapa editables desde la sección Mapa de Ajustes.
    public static int getPocketSize() {
        return PocketMapConfig.pocketSize();
    }

    public static int getHalfSize() {
        return PocketMapConfig.halfSize();
    }

    public static int getPocketSpacing() {
        return PocketMapConfig.pocketSpacing();
    }

    public static int getFloorY() {
        return PocketMapConfig.floorY();
    }

    public static int getSafeY() {
        return PocketMapConfig.safeY();
    }

    public static int getIslandRadius() {
        return PocketMapConfig.islandRadius();
    }

    public static int getIslandThickness() {
        return PocketMapConfig.islandThickness();
    }

    public static int getBorderHeight() {
        return PocketMapConfig.borderHeight();
    }

    public static int getRiftDurationTicks() {
        return PocketMapConfig.riftDurationTicks();
    }

    public static int getRiftWidth() {
        return PocketMapConfig.riftWidth();
    }

    public static int getRiftHeight() {
        return PocketMapConfig.riftHeight();
    }

    public static float getVoidShearDamage() {
        return PocketMapConfig.voidShearDamage();
    }
}
