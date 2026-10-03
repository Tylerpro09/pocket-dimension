package com.fosder.pocketdimension.network;

import com.fosder.pocketdimension.util.PocketMapConfig;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/** Envía al servidor los valores validados del editor de mapas. */
public final class MapConfigPacket {
    private final int pocketSize;
    private final int pocketSpacing;
    private final int floorY;
    private final int islandRadius;
    private final int islandThickness;
    private final int borderHeight;
    private final int riftDurationSeconds;
    private final int riftWidth;
    private final int riftHeight;
    private final double voidDamage;

    public MapConfigPacket(int pocketSize, int pocketSpacing, int floorY, int islandRadius,
                           int islandThickness, int borderHeight, int riftDurationSeconds,
                           int riftWidth, int riftHeight, double voidDamage) {
        this.pocketSize = pocketSize;
        this.pocketSpacing = pocketSpacing;
        this.floorY = floorY;
        this.islandRadius = islandRadius;
        this.islandThickness = islandThickness;
        this.borderHeight = borderHeight;
        this.riftDurationSeconds = riftDurationSeconds;
        this.riftWidth = riftWidth;
        this.riftHeight = riftHeight;
        this.voidDamage = voidDamage;
    }

    public static void encode(MapConfigPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.pocketSize);
        buffer.writeInt(packet.pocketSpacing);
        buffer.writeInt(packet.floorY);
        buffer.writeInt(packet.islandRadius);
        buffer.writeInt(packet.islandThickness);
        buffer.writeInt(packet.borderHeight);
        buffer.writeInt(packet.riftDurationSeconds);
        buffer.writeInt(packet.riftWidth);
        buffer.writeInt(packet.riftHeight);
        buffer.writeDouble(packet.voidDamage);
    }

    public static MapConfigPacket decode(PacketBuffer buffer) {
        return new MapConfigPacket(
                buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readDouble()
        );
    }

    public static void handle(MapConfigPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player != null) {
                PocketMapConfig.applyValues(
                        packet.pocketSize, packet.pocketSpacing, packet.floorY,
                        packet.islandRadius, packet.islandThickness, packet.borderHeight,
                        packet.riftDurationSeconds, packet.riftWidth, packet.riftHeight,
                        packet.voidDamage
                );
            }
        });
        context.setPacketHandled(true);
    }
}
