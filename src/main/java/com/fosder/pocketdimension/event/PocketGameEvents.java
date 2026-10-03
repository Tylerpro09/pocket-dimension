package com.fosder.pocketdimension.event;

import com.fosder.pocketdimension.PocketDimensionMod;
import com.fosder.pocketdimension.util.PocketConfig;
import com.fosder.pocketdimension.util.PocketEditorMode;
import com.fosder.pocketdimension.util.PocketStabilityData;
import com.fosder.pocketdimension.util.PocketText;
import com.fosder.pocketdimension.world.PocketDarkCollapseEngine;
import com.fosder.pocketdimension.world.PocketDimension;
import com.fosder.pocketdimension.world.PocketMath;
import com.fosder.pocketdimension.world.PocketOverworldHellEngine;
import com.fosder.pocketdimension.world.PocketPhysicsEngine;
import com.fosder.pocketdimension.world.PocketPortalEngine;
import com.fosder.pocketdimension.world.PocketWorldBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PocketDimensionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PocketGameEvents {
    private PocketGameEvents() {}

    @SubscribeEvent
    public static void onSpawnCheck(LivingSpawnEvent.CheckSpawn event) {
        if (event.getWorld() instanceof World && PocketDimension.isPocketWorld((World) event.getWorld())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        if (!PocketDimension.isPocketWorld(world)) return;

        Entity entity = event.getEntity();
        if (entity instanceof MobEntity) {
            event.setCanceled(true);
            entity.remove();
        }
    }


    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isClientSide) return;
        if (event.world instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.world;
            PocketPortalEngine.tickTemporaryRifts(world);
            PocketOverworldHellEngine.tick(world);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;

        PocketEditorMode.enforce(player);

        // Sistema de energía profesional: corre con gameTime de Minecraft, no con tiempo real del PC.
        if (player.tickCount % 20 == 0) {
            PocketStabilityData.serverTick(player);
            if (PocketEditorMode.isActive(player)) {
                PocketEditorMode.consumeFlightEnergy(player);
            } else {
                PocketDarkCollapseEngine.tickPlayer(player);
            }
        }

        if (!PocketDimension.isPocketWorld(player.level)) return;

        ServerWorld world = (ServerWorld) player.level;

        java.util.UUID currentOwner = player.getPersistentData().hasUUID("PocketCurrentOwner")
                ? player.getPersistentData().getUUID("PocketCurrentOwner")
                : player.getUUID();

        // Solo genera o repara el bolsillo actual, nunca el resto de la dimensión.
        BlockPos center = PocketWorldBuilder.ensurePocket(world, currentOwner);

        boolean outside = PocketMath.outsidePocket(center, player.getX(), player.getZ());
        boolean fallingVoid = player.getY() < center.getY() - PocketConfig.getIslandThickness() - 8;

        if (outside || fallingVoid) {
            if (!PocketEditorMode.isActive(player)) {
                player.hurt(DamageSource.MAGIC, PocketConfig.getVoidShearDamage());
                player.teleportTo(world, center.getX() + 0.5D, PocketConfig.getSafeY(), center.getZ() + 0.5D, player.yRot, player.xRot);
                if (outside) PocketText.send(player, "message.pocketdimension.border.void");
                if (fallingVoid) PocketText.send(player, "message.pocketdimension.border.rescue");
            }
        }

        if (PocketEditorMode.isActive(player)) return;

        int stability = PocketStabilityData.getStabilityPercent(player);
        applyLowStabilityEffects(player, stability);
        PocketPhysicsEngine.tickPlayer(player, world, center, stability);
        if (player.tickCount % 200 == 0 && PocketStabilityData.isMachineLinked(player)) {
            if (stability <= 0) {
                PocketText.send(player, "message.pocketdimension.stability.machine_off");
            } else if (stability < 25) {
                PocketText.send(player, "message.pocketdimension.stability.critical", stability);
            } else if (stability < 50) {
                PocketText.send(player, "message.pocketdimension.stability.degrading", stability);
            }
        }
    }

    private static void applyLowStabilityEffects(ServerPlayerEntity player, int stability) {
        if (player.isCreative()) return;
        if (stability >= PocketConfig.LOW_STABILITY_PERCENT) return;

        player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 60, 0, true, false));

        if (stability < PocketConfig.CRITICAL_STABILITY_PERCENT) {
            player.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 80, 0, true, false));
            player.addEffect(new EffectInstance(Effects.HUNGER, 80, 0, true, false));
            if (player.tickCount % 100 == 0) {
                player.hurt(DamageSource.MAGIC, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onEditorDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
            if (PocketEditorMode.isActive(player)) {
                event.setCanceled(true);
                player.setHealth(1.0F);
                PocketEditorMode.enforce(player);
            }
        }
    }

    @SubscribeEvent
    public static void onEditorFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity
                && PocketEditorMode.isActive((ServerPlayerEntity) event.getEntityLiving())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            PocketEditorMode.setActive((ServerPlayerEntity) event.getPlayer(), false);
        }
    }
}
