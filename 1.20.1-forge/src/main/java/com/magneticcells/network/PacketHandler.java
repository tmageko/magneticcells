package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL = "1";
    private static int id = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MagneticCellsMod.MODID, "main"),
        () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
    );

    public static void register() {
        CHANNEL.registerMessage(id++, PacketToggleFilterMode.class,
            PacketToggleFilterMode::encode, PacketToggleFilterMode::decode, PacketToggleFilterMode::handle);
        CHANNEL.registerMessage(id++, PacketSetFilterSlot.class,
            PacketSetFilterSlot::encode, PacketSetFilterSlot::decode, PacketSetFilterSlot::handle);
        CHANNEL.registerMessage(id++, PacketExtractItem.class,
            PacketExtractItem::encode, PacketExtractItem::decode, PacketExtractItem::handle);
        CHANNEL.registerMessage(id++, PacketSyncContents.class,
            PacketSyncContents::encode, PacketSyncContents::decode, PacketSyncContents::handle);
        CHANNEL.registerMessage(id++, PacketInsertItem.class,
            PacketInsertItem::encode, PacketInsertItem::decode, PacketInsertItem::handle);
        CHANNEL.registerMessage(id++, PacketToggleMagnet.class,
            PacketToggleMagnet::encode, PacketToggleMagnet::decode, PacketToggleMagnet::handle);
    }

    public static <MSG> void sendToServer(MSG msg) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), msg);
    }
}
