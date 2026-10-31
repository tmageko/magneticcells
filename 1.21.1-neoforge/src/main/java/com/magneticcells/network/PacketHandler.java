package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar reg = event.registrar(MagneticCellsMod.MODID).versioned("1");

        reg.playToServer(PacketToggleFilterMode.TYPE, PacketToggleFilterMode.STREAM_CODEC,
            PacketToggleFilterMode::handle);
        reg.playToServer(PacketSetFilterSlot.TYPE, PacketSetFilterSlot.STREAM_CODEC,
            PacketSetFilterSlot::handle);
        reg.playToServer(PacketExtractItem.TYPE, PacketExtractItem.STREAM_CODEC,
            PacketExtractItem::handle);
        reg.playToServer(PacketInsertItem.TYPE, PacketInsertItem.STREAM_CODEC,
            PacketInsertItem::handle);
        reg.playToServer(PacketToggleMagnet.TYPE, PacketToggleMagnet.STREAM_CODEC,
            PacketToggleMagnet::handle);
        reg.playToServer(PacketUpgradeAction.TYPE, PacketUpgradeAction.STREAM_CODEC,
            PacketUpgradeAction::handle);
        reg.playToServer(PacketVoidFilterAction.TYPE, PacketVoidFilterAction.STREAM_CODEC,
            PacketVoidFilterAction::handle);

        reg.playToClient(PacketSyncContents.TYPE, PacketSyncContents.STREAM_CODEC,
            PacketSyncContents::handle);
    }

    public static <MSG extends net.minecraft.network.protocol.common.custom.CustomPacketPayload>
    void sendToServer(MSG msg) {
        PacketDistributor.sendToServer(msg);
    }

    public static <MSG extends net.minecraft.network.protocol.common.custom.CustomPacketPayload>
    void sendToPlayer(net.minecraft.server.level.ServerPlayer player, MSG msg) {
        PacketDistributor.sendToPlayer(player, msg);
    }
}
