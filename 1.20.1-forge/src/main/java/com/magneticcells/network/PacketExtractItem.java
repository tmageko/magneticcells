package com.magneticcells.network;

import com.magneticcells.gui.MagneticCellMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketExtractItem {

    private final int contentIndex;
    private final boolean extractAll;

    public PacketExtractItem(int contentIndex, boolean extractAll) {
        this.contentIndex = contentIndex;
        this.extractAll   = extractAll;
    }

    public static void encode(PacketExtractItem pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.contentIndex);
        buf.writeBoolean(pkt.extractAll);
    }

    public static PacketExtractItem decode(FriendlyByteBuf buf) {
        return new PacketExtractItem(buf.readInt(), buf.readBoolean());
    }

    public static void handle(PacketExtractItem pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (player.containerMenu instanceof MagneticCellMenu menu) {
                if (pkt.extractAll) {
                    menu.extractAll(pkt.contentIndex, player);
                } else {
                    menu.extractOne(pkt.contentIndex, player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
