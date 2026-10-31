package com.magneticcells.network;

import com.magneticcells.gui.MagneticCellMenu;
import com.magneticcells.items.VoidUpgradeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpgradeAction {

    public static final int INSERT  = 0;
    public static final int EXTRACT = 1;

    private final int action;

    public PacketUpgradeAction(int action) { this.action = action; }

    public static void encode(PacketUpgradeAction p, FriendlyByteBuf buf) { buf.writeInt(p.action); }
    public static PacketUpgradeAction decode(FriendlyByteBuf buf) { return new PacketUpgradeAction(buf.readInt()); }

    public static void handle(PacketUpgradeAction pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof MagneticCellMenu menu)) return;

            if (pkt.action == INSERT) {
                // Вставляем из руки если это VoidUpgrade и слот пустой
                ItemStack carried = player.containerMenu.getCarried();
                if (!carried.isEmpty() && carried.getItem() instanceof VoidUpgradeItem
                        && menu.getUpgradeItem().isEmpty()) {
                    menu.setUpgradeItem(carried.copyWithCount(1));
                    carried.shrink(1);
                    player.containerMenu.broadcastChanges();
                }
            } else if (pkt.action == EXTRACT) {
                // Извлекаем в руку или инвентарь
                ItemStack current = menu.getUpgradeItem();
                if (!current.isEmpty()) {
                    if (player.containerMenu.getCarried().isEmpty()) {
                        player.containerMenu.setCarried(current.copy());
                    } else {
                        player.getInventory().add(current.copy());
                    }
                    menu.setUpgradeItem(ItemStack.EMPTY);
                    player.containerMenu.broadcastChanges();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
