package com.fosder.pocketdimension.network;

import com.fosder.pocketdimension.util.PocketEditorMode;
import com.fosder.pocketdimension.util.PocketTeleport;
import com.fosder.pocketdimension.util.PocketText;
import com.fosder.pocketdimension.world.PocketDimension;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/** Viaje desde el editor al bolsillo y de regreso usando el coste normal. */
public final class EditorTravelPacket {
    public static void encode(EditorTravelPacket packet, PacketBuffer buffer) {
        // Paquete sin datos: la acción siempre usa el bolsillo del jugador.
    }

    public static EditorTravelPacket decode(PacketBuffer buffer) {
        return new EditorTravelPacket();
    }

    public static void handle(EditorTravelPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player == null || !PocketEditorMode.isActive(player)) return;

            if (PocketDimension.isPocketWorld(player.level)) {
                if (PocketTeleport.returnBackFromEditor(player)) {
                    PocketText.send(player, "message.pocketdimension.key_returned");
                }
            } else if (PocketTeleport.enterPocketFromEditor(player)) {
                PocketText.send(player, "message.pocketdimension.key_entered");
            }
        });
        context.setPacketHandled(true);
    }
}
