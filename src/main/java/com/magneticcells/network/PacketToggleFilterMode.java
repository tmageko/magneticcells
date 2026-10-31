package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.gui.MagneticCellMenu;
import com.magneticcells.items.CellData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToggleFilterMode() implements CustomPacketPayload {

    public static final Type<PacketToggleFilterMode> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MagneticCellsMod.MODID, "toggle_filter_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketToggleFilterMode> STREAM_CODEC =
        StreamCodec.unit(new PacketToggleFilterMode());

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketToggleFilterMode pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.containerMenu instanceof MagneticCellMenu menu) {
                ItemStack cell = menu.getCellStack();
                if (!cell.isEmpty()) CellData.toggleFilterMode(cell);
            }
        });
    }
}
