package com.fosder.pocketdimension.util;

import com.fosder.pocketdimension.world.PocketDimension;
import com.fosder.pocketdimension.world.PocketWorldBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.UUID;

public final class PocketTeleport {
    private PocketTeleport() {}

    public static boolean enterPocket(ServerPlayerEntity player, UUID owner, CompoundNBT storage) {
        return enterPocket(player, owner, storage, PocketConfig.DIRECT_TELEPORT_ENERGY_COST);
    }

    public static boolean enterPocketFromRift(ServerPlayerEntity player, UUID owner, CompoundNBT storage) {
        return enterPocket(player, owner, storage, PocketConfig.RIFT_TRAVEL_ENERGY_COST);
    }

    public static boolean returnBack(ServerPlayerEntity player, CompoundNBT storage) {
        return returnBack(player, storage, PocketConfig.DIRECT_TELEPORT_ENERGY_COST);
    }

    public static boolean returnBackFromRift(ServerPlayerEntity player, CompoundNBT storage) {
        return returnBack(player, storage, PocketConfig.RIFT_TRAVEL_ENERGY_COST);
    }

    public static boolean enterPocketFromEditor(ServerPlayerEntity player) {
        return enterPocket(player, player.getUUID(), new CompoundNBT(), PocketConfig.DIRECT_TELEPORT_ENERGY_COST);
    }

    public static boolean returnBackFromEditor(ServerPlayerEntity player) {
        return returnBack(player, new CompoundNBT(), PocketConfig.DIRECT_TELEPORT_ENERGY_COST);
    }

    private static boolean enterPocket(ServerPlayerEntity player, UUID owner, CompoundNBT storage, int energyCost) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        ServerWorld pocketWorld = server.getLevel(PocketDimension.POCKET_WORLD);
        if (pocketWorld == null) {
            PocketText.send(player, "message.pocketdimension.teleport.world_missing");
            return false;
        }
        if (!chargeTravelEnergy(player, energyCost)) return false;

        // Return coordinates belong to the player, not to a key stack. This
        // prevents shared, traded or duplicated keys from sharing locations.
        saveReturnPoint(player);
        storage.putUUID("PocketOwner", owner);
        player.getPersistentData().putUUID("PocketCurrentOwner", owner);

        BlockPos center = PocketWorldBuilder.ensurePocket(pocketWorld, owner);
        player.teleportTo(pocketWorld, center.getX() + 0.5D, PocketConfig.getSafeY(), center.getZ() + 0.5D, player.yRot, player.xRot);
        return true;
    }

    private static boolean returnBack(ServerPlayerEntity player, CompoundNBT storage, int energyCost) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        if (!chargeTravelEnergy(player, energyCost)) return false;

        ServerWorld target = null;
        double x;
        double y;
        double z;
        float yaw = player.yRot;
        float pitch = player.xRot;

        CompoundNBT returnData = getReturnStorage(player);
        if (returnData.contains(PocketConfig.TAG_RETURN_DIM)) {
            ResourceLocation loc = new ResourceLocation(returnData.getString(PocketConfig.TAG_RETURN_DIM));
            RegistryKey<World> key = RegistryKey.create(Registry.DIMENSION_REGISTRY, loc);
            target = server.getLevel(key);
            x = returnData.getDouble(PocketConfig.TAG_RETURN_X);
            y = returnData.getDouble(PocketConfig.TAG_RETURN_Y);
            z = returnData.getDouble(PocketConfig.TAG_RETURN_Z);
            if (returnData.contains(PocketConfig.TAG_RETURN_YAW)) {
                yaw = returnData.getFloat(PocketConfig.TAG_RETURN_YAW);
            }
            if (returnData.contains(PocketConfig.TAG_RETURN_PITCH)) {
                pitch = returnData.getFloat(PocketConfig.TAG_RETURN_PITCH);
            }
        } else {
            target = server.getLevel(World.OVERWORLD);
            if (target == null) return false;
            BlockPos spawn = target.getSharedSpawnPos();
            x = spawn.getX() + 0.5D;
            y = spawn.getY() + 1.0D;
            z = spawn.getZ() + 0.5D;
        }

        if (target == null) {
            PocketText.send(player, "message.pocketdimension.teleport.return_world_missing");
            target = server.getLevel(World.OVERWORLD);
            if (target == null) return false;
            BlockPos spawn = target.getSharedSpawnPos();
            x = spawn.getX() + 0.5D;
            y = spawn.getY() + 1.0D;
            z = spawn.getZ() + 0.5D;
        }

        player.teleportTo(target, x, y, z, yaw, pitch);
        player.getPersistentData().remove("PocketCurrentOwner");
        clearReturnPoint(player);
        return true;
    }

    private static boolean chargeTravelEnergy(ServerPlayerEntity player, int amount) {
        if (player.isCreative()) return true;
        int totalCost = amount + carriedMassEnergyCost(player);
        if (PocketStabilityData.tryConsumeEnergy(player, totalCost)) return true;
            PocketText.send(player, "message.pocketdimension.teleport.energy_missing");
        return false;
    }

    private static int carriedMassEnergyCost(ServerPlayerEntity player) {
        int itemCount = 0;
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty()) itemCount += stack.getCount();
        }

        int armorPieces = 0;
        for (ItemStack stack : player.inventory.armor) {
            if (!stack.isEmpty()) armorPieces++;
        }

        int itemCost = itemCount / Math.max(1, PocketConfig.ITEM_MASS_PER_ENERGY);
        int armorCost = armorPieces * PocketConfig.ARMOR_MASS_ENERGY_COST;
        return itemCost + armorCost;
    }

    public static void saveReturnPoint(PlayerEntity player) {
        if (player == null || player.level == null) return;
        CompoundNBT storage = getReturnStorage(player);
        storage.putString(PocketConfig.TAG_RETURN_DIM, player.level.dimension().location().toString());
        storage.putDouble(PocketConfig.TAG_RETURN_X, player.getX());
        storage.putDouble(PocketConfig.TAG_RETURN_Y, player.getY());
        storage.putDouble(PocketConfig.TAG_RETURN_Z, player.getZ());
        storage.putFloat(PocketConfig.TAG_RETURN_YAW, player.yRot);
        storage.putFloat(PocketConfig.TAG_RETURN_PITCH, player.xRot);
    }

    private static CompoundNBT getReturnStorage(PlayerEntity player) {
        CompoundNBT persistent = player.getPersistentData();
        CompoundNBT storage = persistent.getCompound(PocketConfig.TAG_ROOT);
        persistent.put(PocketConfig.TAG_ROOT, storage);
        return storage;
    }

    private static void clearReturnPoint(ServerPlayerEntity player) {
        CompoundNBT storage = getReturnStorage(player);
        storage.remove(PocketConfig.TAG_RETURN_DIM);
        storage.remove(PocketConfig.TAG_RETURN_X);
        storage.remove(PocketConfig.TAG_RETURN_Y);
        storage.remove(PocketConfig.TAG_RETURN_Z);
        storage.remove(PocketConfig.TAG_RETURN_YAW);
        storage.remove(PocketConfig.TAG_RETURN_PITCH);
    }
}
