package com.fosder.pocketdimension.network;

import com.fosder.pocketdimension.util.PocketEditorMode;
import com.fosder.pocketdimension.util.PocketMaterialShop;
import com.fosder.pocketdimension.util.PocketStabilityData;
import com.fosder.pocketdimension.util.PocketText;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/** Compra validada en servidor para la tienda del modo editor. */
public final class MaterialShopPacket {
    private final int entryId;

    public MaterialShopPacket(int entryId) {
        this.entryId = entryId;
    }

    public static void encode(MaterialShopPacket packet, net.minecraft.network.PacketBuffer buffer) {
        buffer.writeVarInt(packet.entryId);
    }

    public static MaterialShopPacket decode(net.minecraft.network.PacketBuffer buffer) {
        return new MaterialShopPacket(buffer.readVarInt());
    }

    public static void handle(MaterialShopPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player == null) return;

            if (!PocketEditorMode.isActive(player)) {
                PocketText.send(player, "message.pocketdimension.shop.unavailable");
                return;
            }
            if (!PocketMaterialShop.isValid(packet.entryId)) {
                PocketText.send(player, "message.pocketdimension.shop.unavailable");
                return;
            }

            int cost = PocketMaterialShop.cost(packet.entryId);
            ItemStack stack = PocketMaterialShop.createStack(packet.entryId);
            if (stack.isEmpty()) return;

            if (!PocketStabilityData.tryConsumeEnergy(player, cost)) {
                PocketText.send(player, "message.pocketdimension.shop.energy_missing", cost,
                        PocketStabilityData.getEnergy(player));
                return;
            }

            if (!player.inventory.add(stack)) {
                PocketStabilityData.addEnergy(player, cost);
                PocketText.send(player, "message.pocketdimension.shop.inventory_full");
                return;
            }

            PocketText.send(player, "message.pocketdimension.shop.purchased",
                    stack.getHoverName(), stack.getCount(), cost,
                    PocketStabilityData.getEnergy(player));
        });
        context.setPacketHandled(true);
    }
}
