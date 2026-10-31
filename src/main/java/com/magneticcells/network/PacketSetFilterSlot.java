package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.gui.MagneticCellMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSetFilterSlot(int filterIndex, ItemStack stack) implements CustomPacketPayload {

    public static final Type<PacketSetFilterSlot> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MagneticCellsMod.MODID, "set_filter_slot"));

    // Кодируем ItemStack просто как строку ID (без HolderLookup)
    private static final StreamCodec<FriendlyByteBuf, ItemStack> ITEM_CODEC =
        StreamCodec.of(
            (buf, stack) -> buf.writeUtf(stack.isEmpty() ? "" :
                BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()),
            buf -> {
                String id = buf.readUtf();
                if (id.isEmpty()) return ItemStack.EMPTY;
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if (rl == null) return ItemStack.EMPTY;
                var item = BuiltInRegistries.ITEM.get(rl);
                return item == null ? ItemStack.EMPTY : new ItemStack(item);
            }
        );

    public static final StreamCodec<FriendlyByteBuf, PacketSetFilterSlot> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT,  PacketSetFilterSlot::filterIndex,
            ITEM_CODEC,         PacketSetFilterSlot::stack,
            PacketSetFilterSlot::new
        );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSetFilterSlot pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (!(player.containerMenu instanceof MagneticCellMenu menu)) return;
            menu.setFilterSlot(pkt.filterIndex,
                pkt.stack.isEmpty() ? ItemStack.EMPTY : pkt.stack.copyWithCount(1));
        });
    }
}
