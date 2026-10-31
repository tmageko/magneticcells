package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.gui.MagneticCellMenu;
import com.magneticcells.items.CellData;
import com.magneticcells.items.MagneticStorageCellItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketInsertItem(int inventorySlot) implements CustomPacketPayload {

    public static final Type<PacketInsertItem> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MagneticCellsMod.MODID, "insert_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketInsertItem> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.INT, PacketInsertItem::inventorySlot, PacketInsertItem::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketInsertItem pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
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

            if (fromCarried) player.containerMenu.setCarried(leftover.isEmpty() ? ItemStack.EMPTY : leftover);
            else             source.shrink(inserted);

            CellData.markDirty(player.getInventory(), menu.cellSlotIndex);
            menu.syncToClient(player);
            player.containerMenu.broadcastChanges();
        });
    }
}
