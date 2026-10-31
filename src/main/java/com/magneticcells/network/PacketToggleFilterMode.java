package com.magneticcells.network;

import com.magneticcells.gui.MagneticCellMenu;
import com.magneticcells.items.CellData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleFilterMode {

    public PacketToggleFilterMode() {}

    public static void encode(PacketToggleFilterMode pkt, FriendlyByteBuf buf) {}

    public static PacketToggleFilterMode decode(FriendlyByteBuf buf) {
        return new PacketToggleFilterMode();
    }

    public static void handle(PacketToggleFilterMode pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (player.containerMenu instanceof MagneticCellMenu menu) {
                ItemStack cell = menu.getCellStack();
                if (!cell.isEmpty()) {
                    CellData.toggleFilterMode(cell);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
