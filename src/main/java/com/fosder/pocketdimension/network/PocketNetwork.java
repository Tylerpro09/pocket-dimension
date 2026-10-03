package com.fosder.pocketdimension.network;

import com.fosder.pocketdimension.PocketDimensionMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/** Canal común para las acciones del editor realizadas desde la GUI. */
public final class PocketNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PocketDimensionMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextMessageId;

    private PocketNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(
                nextMessageId++,
                EditorModePacket.class,
                EditorModePacket::encode,
                EditorModePacket::decode,
                EditorModePacket::handle
        );
        CHANNEL.registerMessage(
                nextMessageId++,
                MapConfigPacket.class,
                MapConfigPacket::encode,
                MapConfigPacket::decode,
                MapConfigPacket::handle
        );
        CHANNEL.registerMessage(
                nextMessageId++,
                EditorTravelPacket.class,
                EditorTravelPacket::encode,
                EditorTravelPacket::decode,
                EditorTravelPacket::handle
        );
        CHANNEL.registerMessage(
                nextMessageId++,
                MaterialShopPacket.class,
                MaterialShopPacket::encode,
                MaterialShopPacket::decode,
                MaterialShopPacket::handle
        );
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
