package com.magneticcells.network;

import com.magneticcells.items.CellData;
import com.magneticcells.items.MagneticStorageCellItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleMagnet {

    public PacketToggleMagnet() {}

    public static void encode(PacketToggleMagnet pkt, FriendlyByteBuf buf) {}
    public static PacketToggleMagnet decode(FriendlyByteBuf buf) { return new PacketToggleMagnet(); }

    public static void handle(PacketToggleMagnet pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (!s.isEmpty() && s.getItem() instanceof MagneticStorageCellItem) {
                    CellData.toggleMagnet(s);
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
