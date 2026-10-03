package com.fosder.pocketdimension.item;

import com.fosder.pocketdimension.util.PocketTeleport;
import com.fosder.pocketdimension.util.PocketText;
import com.fosder.pocketdimension.world.PocketDimension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class HackerKeyItem extends Item {
    public HackerKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) return new ActionResult<>(ActionResultType.SUCCESS, stack);
        if (!(player instanceof ServerPlayerEntity)) return new ActionResult<>(ActionResultType.PASS, stack);

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        CompoundNBT tag = stack.getOrCreateTag();

        if (player.isShiftKeyDown()) {
            ServerPlayerEntity target = findNearestTarget(serverPlayer);
            if (target == null) {
                PocketText.send(player, "message.pocketdimension.hacker.no_target");
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            tag.putUUID("TargetOwner", target.getUUID());
            tag.putString("TargetName", target.getName().getString());
            PocketText.send(player, "message.pocketdimension.hacker.target_set", target.getName().getString());
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (PocketDimension.isPocketWorld(world)) {
            if (PocketTeleport.returnBack(serverPlayer, tag)) {
                PocketText.send(player, "message.pocketdimension.hacker.left_dimension");
            }
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (!tag.hasUUID("TargetOwner")) {
            PocketText.send(player, "message.pocketdimension.hacker.set_target_hint");
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        UUID targetId = tag.getUUID("TargetOwner");
        ServerPlayerEntity target = findOnlineById(serverPlayer.getServer(), targetId);
        if (target == null) {
            PocketText.send(player, "message.pocketdimension.hacker.target_offline");
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        MinecraftServer server = serverPlayer.getServer();
        if (server != null && server.getPlayerList().isOp(target.getGameProfile())) {
            PocketText.send(player, "message.pocketdimension.hacker.op_blocked");
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (PocketTeleport.enterPocket(serverPlayer, targetId, tag)) {
            PocketText.send(player, "message.pocketdimension.hacker.intrusion_started", target.getName().getString());
            PocketText.send(target, "message.pocketdimension.hacker.alert");
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    private static ServerPlayerEntity findNearestTarget(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return null;
        ServerPlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        Vector3d pos = player.position();

        for (ServerPlayerEntity other : server.getPlayerList().getPlayers()) {
            if (other == player) continue;
            double dist = other.position().distanceToSqr(pos);
            if (dist < bestDist && dist <= 128D * 128D) {
                best = other;
                bestDist = dist;
            }
        }
        return best;
    }

    private static ServerPlayerEntity findOnlineById(MinecraftServer server, UUID id) {
        if (server == null) return null;
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            if (player.getUUID().equals(id)) return player;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("TargetName")) {
            tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.hacker_target", tag.getString("TargetName")));
        } else {
            tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.hacker_unset"));
        }
        tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.hacker_op"));
    }
}
