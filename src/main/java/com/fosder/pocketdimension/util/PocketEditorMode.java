package com.fosder.pocketdimension.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameType;

/**
 * Estado seguro del modo editor de mapas.
 *
 * El editor no usa creativo: mantiene al jugador en supervivencia, concede
 * únicamente vuelo y evita que una muerte cierre la sesión de edición.
 */
public final class PocketEditorMode {
    private static final String ACTIVE = "PocketMapEditorActive";
    private static final String PREVIOUS_GAME_TYPE = "PocketMapEditorPreviousGameType";
    private static final String PREVIOUS_MAY_FLY = "PocketMapEditorPreviousMayFly";
    private static final String PREVIOUS_FLYING = "PocketMapEditorPreviousFlying";

    private PocketEditorMode() {}

    public static boolean isActive(net.minecraft.entity.player.PlayerEntity player) {
        return player != null && player.getPersistentData().getBoolean(ACTIVE);
    }

    public static void setActive(ServerPlayerEntity player, boolean active) {
        if (player == null) return;

        if (active) {
            if (!isActive(player)) {
                player.getPersistentData().putInt(PREVIOUS_GAME_TYPE, player.gameMode.getGameModeForPlayer().getId());
                player.getPersistentData().putBoolean(PREVIOUS_MAY_FLY, player.abilities.mayfly);
                player.getPersistentData().putBoolean(PREVIOUS_FLYING, player.abilities.flying);
                player.getPersistentData().putBoolean(ACTIVE, true);
            }

            forceEditorRules(player);
            return;
        }

        if (!isActive(player)) return;

        player.getPersistentData().putBoolean(ACTIVE, false);
        int previousGameType = player.getPersistentData().getInt(PREVIOUS_GAME_TYPE);
        player.setGameMode(GameType.byId(previousGameType, GameType.SURVIVAL));

        player.abilities.mayfly = player.getPersistentData().getBoolean(PREVIOUS_MAY_FLY);
        player.abilities.flying = player.getPersistentData().getBoolean(PREVIOUS_FLYING) && player.abilities.mayfly;
        player.onUpdateAbilities();
    }

    public static void enforce(ServerPlayerEntity player) {
        if (isActive(player)) {
            forceEditorRules(player);
        }
    }

    public static boolean consumeFlightEnergy(ServerPlayerEntity player) {
        if (!isActive(player) || !player.abilities.flying) return true;
        if (PocketStabilityData.tryConsumeEnergy(player, PocketConfig.EDITOR_FLIGHT_ENERGY_PER_SECOND)) {
            return true;
        }

        setActive(player, false);
        PocketText.send(player, "message.pocketdimension.editor.energy_empty");
        return false;
    }

    private static void forceEditorRules(ServerPlayerEntity player) {
        if (player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            // El editor nunca puede quedarse en creativo ni espectador.
            player.setGameMode(GameType.SURVIVAL);
        }

        player.abilities.mayfly = true;
        player.abilities.flying = true;
        if (player.getHealth() <= 0.0F) {
            player.setHealth(1.0F);
        }
        player.onUpdateAbilities();
    }
}
