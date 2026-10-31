package com.magneticcells.network;

import com.magneticcells.gui.MagneticCellMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Клиент → Сервер: установить/очистить слот фильтра.
 * Предмет копируется как образец — из инвентаря НЕ забирается.
 */
public class PacketSetFilterSlot {

    private final int filterIndex;
    private final ItemStack stack; // EMPTY = очистить

    public PacketSetFilterSlot(int filterIndex, ItemStack stack) {
        this.filterIndex = filterIndex;
        this.stack = stack;
    }

    public static void encode(PacketSetFilterSlot pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.filterIndex);
        buf.writeItem(pkt.stack);
    }

    public static PacketSetFilterSlot decode(FriendlyByteBuf buf) {
        return new PacketSetFilterSlot(buf.readInt(), buf.readItem());
    }

    public static void handle(PacketSetFilterSlot pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof MagneticCellMenu menu)) return;
            // Просто запоминаем образец — count=1, инвентарь не трогаем
            menu.setFilterSlot(pkt.filterIndex,
                pkt.stack.isEmpty() ? ItemStack.EMPTY : pkt.stack.copyWithCount(1));
        });
        ctx.get().setPacketHandled(true);
    }
}
