package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.items.CellData;
import com.magneticcells.items.MagneticStorageCellItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToggleMagnet() implements CustomPacketPayload {

    public static final Type<PacketToggleMagnet> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MagneticCellsMod.MODID, "toggle_magnet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketToggleMagnet> STREAM_CODEC =
        StreamCodec.unit(new PacketToggleMagnet());

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketToggleMagnet pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (!s.isEmpty() && s.getItem() instanceof MagneticStorageCellItem) {
                    CellData.toggleMagnet(s);
                    break;
                }
            }
        });
    }
}
