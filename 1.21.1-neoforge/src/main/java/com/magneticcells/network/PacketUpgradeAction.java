package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.gui.MagneticCellMenu;
import com.magneticcells.items.VoidUpgradeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpgradeAction(int action) implements CustomPacketPayload {

    public static final int INSERT  = 0;
    public static final int EXTRACT = 1;

    public static final Type<PacketUpgradeAction> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MagneticCellsMod.MODID, "upgrade_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpgradeAction> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.INT, PacketUpgradeAction::action, PacketUpgradeAction::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketUpgradeAction pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (!(player.containerMenu instanceof MagneticCellMenu menu)) return;
            if (pkt.action == INSERT) {
                ItemStack carried = player.containerMenu.getCarried();
                if (!carried.isEmpty() && carried.getItem() instanceof VoidUpgradeItem
                        && menu.getUpgradeItem().isEmpty()) {
                    menu.setUpgradeItem(carried.copyWithCount(1));
                    carried.shrink(1);
                    player.containerMenu.broadcastChanges();
                }
            } else if (pkt.action == EXTRACT) {
                ItemStack current = menu.getUpgradeItem();
                if (!current.isEmpty()) {
                    if (player.containerMenu.getCarried().isEmpty())
                        player.containerMenu.setCarried(current.copy());
                    else
                        player.getInventory().add(current.copy());
                    menu.setUpgradeItem(ItemStack.EMPTY);
                    player.containerMenu.broadcastChanges();
                }
            }
        });
    }
}
