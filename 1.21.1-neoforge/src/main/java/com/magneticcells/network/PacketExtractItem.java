package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.gui.MagneticCellMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketExtractItem(int contentIndex, boolean extractAll) implements CustomPacketPayload {

    public static final Type<PacketExtractItem> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MagneticCellsMod.MODID, "extract_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketExtractItem> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT,     PacketExtractItem::contentIndex,
            ByteBufCodecs.BOOL,    PacketExtractItem::extractAll,
            PacketExtractItem::new
        );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketExtractItem pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.containerMenu instanceof MagneticCellMenu menu) {
                if (pkt.extractAll) menu.extractAll(pkt.contentIndex, player);
                else                menu.extractOne(pkt.contentIndex, player);
            }
        });
    }
}
