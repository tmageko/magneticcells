package com.magneticcells.network;

import com.magneticcells.gui.MagneticCellMenu;
import com.magneticcells.items.CellData;
import com.magneticcells.items.MagneticStorageCellItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketInsertItem {

    private final int inventorySlot; // -1 = из руки

    public PacketInsertItem(int inventorySlot) { this.inventorySlot = inventorySlot; }

    public static void encode(PacketInsertItem pkt, FriendlyByteBuf buf) { buf.writeInt(pkt.inventorySlot); }
    public static PacketInsertItem decode(FriendlyByteBuf buf) { return new PacketInsertItem(buf.readInt()); }

    public static void handle(PacketInsertItem pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof MagneticCellMenu menu)) return;

            ItemStack cell = menu.getCellStack();
            if (cell.isEmpty() || !(cell.getItem() instanceof MagneticStorageCellItem ci)) return;

            boolean fromCarried = pkt.inventorySlot == -1;
            ItemStack source = fromCarried
                ? player.containerMenu.getCarried()
                : (pkt.inventorySlot >= 0 && pkt.inventorySlot < player.getInventory().getContainerSize()
                    ? player.getInventory().getItem(pkt.inventorySlot) : ItemStack.EMPTY);

            if (source.isEmpty() || !CellData.isAllowed(cell, source)) return;

            int countBefore = source.getCount();
            ItemStack leftover = CellData.insertItem(cell, source.copy(), ci.getMaxItems(), ci.getMaxTypes());
            int inserted = countBefore - leftover.getCount();
            if (inserted <= 0) return;

            if (fromCarried) {
                player.containerMenu.setCarried(leftover.isEmpty() ? ItemStack.EMPTY : leftover);
            } else {
                source.shrink(inserted);
            }

            menu.syncToClient(player);
            player.containerMenu.broadcastChanges();
        });
        ctx.get().setPacketHandled(true);
    }
}
