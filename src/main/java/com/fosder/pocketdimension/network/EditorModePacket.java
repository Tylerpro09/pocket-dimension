package com.fosder.pocketdimension.network;

import com.fosder.pocketdimension.util.PocketEditorMode;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/** Solicitud cliente-servidor para entrar o salir del editor de mapas. */
public final class EditorModePacket {
    private final boolean active;

    public EditorModePacket(boolean active) {
        this.active = active;
    }

    public static void encode(EditorModePacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.active);
    }

    public static EditorModePacket decode(PacketBuffer buffer) {
        return new EditorModePacket(buffer.readBoolean());
    }

    public static void handle(EditorModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player != null) {
                PocketEditorMode.setActive(player, packet.active);
            }
        });
        context.setPacketHandled(true);
    }
}
