package com.magneticcells.network;

import com.magneticcells.gui.MagneticCellMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Сервер → Клиент: сигнал что данные ячейки обновились.
 * Клиент читает данные напрямую из NBT ItemStack в инвентаре.
 */
public class PacketSyncContents {

    private final int energy;
    private final int maxEnergy;

    public PacketSyncContents(ItemStack[] ignored, int energy, int maxEnergy) {
        this.energy    = energy;
        this.maxEnergy = maxEnergy;
    }

    public static void encode(PacketSyncContents pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.energy);
        buf.writeInt(pkt.maxEnergy);
    }

    public static PacketSyncContents decode(FriendlyByteBuf buf) {
        return new PacketSyncContents(null, buf.readInt(), buf.readInt());
    }

    public static void handle(PacketSyncContents pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu instanceof MagneticCellMenu menu) {
                menu.clientEnergy    = pkt.energy;
                menu.clientMaxEnergy = pkt.maxEnergy;
                // Данные читаются напрямую из NBT — ничего не нужно кешировать
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
