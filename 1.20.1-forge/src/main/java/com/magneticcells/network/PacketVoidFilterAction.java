package com.magneticcells.network;

import com.magneticcells.gui.MagneticCellMenu;
import com.magneticcells.items.CellData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Управление фильтром Void Upgrade:
 * action=0 → установить слот (idx, stack)
 * action=1 → переключить режим whitelist/blacklist
 */
public class PacketVoidFilterAction {

    public static final int SET_SLOT   = 0;
    public static final int TOGGLE_MODE = 1;

    private static final String FILTER_TAG  = "VoidFilter";
    private static final String MODE_TAG    = "VoidMode";
    private static final int    FILTER_SIZE = 9;

    private final int action;
    private final int slotIdx;
    private final ItemStack stack;

    public PacketVoidFilterAction(int action, int slotIdx, ItemStack stack) {
        this.action  = action;
        this.slotIdx = slotIdx;
        this.stack   = stack;
    }

    public static void encode(PacketVoidFilterAction p, FriendlyByteBuf buf) {
        buf.writeInt(p.action);
        buf.writeInt(p.slotIdx);
        buf.writeItem(p.stack);
    }
    public static PacketVoidFilterAction decode(FriendlyByteBuf buf) {
        return new PacketVoidFilterAction(buf.readInt(), buf.readInt(), buf.readItem());
    }

    public static void handle(PacketVoidFilterAction pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof MagneticCellMenu menu)) return;

            ItemStack upgrade = menu.getUpgradeItem();
            if (upgrade.isEmpty()) return;

            CompoundTag tag = upgrade.getOrCreateTag();

            if (pkt.action == SET_SLOT) {
                // Читаем или создаём список фильтра
                ListTag list = tag.contains(FILTER_TAG, Tag.TAG_LIST)
                    ? tag.getList(FILTER_TAG, Tag.TAG_COMPOUND)
                    : new ListTag();
                // Расширяем до нужного размера
                while (list.size() < FILTER_SIZE) list.add(new CompoundTag());
                // Устанавливаем слот
                if (pkt.slotIdx >= 0 && pkt.slotIdx < FILTER_SIZE) {
                    CompoundTag entry = new CompoundTag();
                    if (!pkt.stack.isEmpty()) pkt.stack.copyWithCount(1).save(entry);
                    list.set(pkt.slotIdx, entry);
                }
                tag.put(FILTER_TAG, list);
                menu.setUpgradeItem(upgrade); // сохраняем обратно в NBT ячейки
            } else if (pkt.action == TOGGLE_MODE) {
                int cur = tag.getInt(MODE_TAG);
                tag.putInt(MODE_TAG, cur == 0 ? 1 : 0);
                menu.setUpgradeItem(upgrade);
            }
            player.containerMenu.broadcastChanges();
        });
        ctx.get().setPacketHandled(true);
    }

    // ── Хелперы для чтения данных на клиенте ──

    public static ItemStack getFilterSlot(ItemStack upgrade, int idx) {
        if (upgrade.isEmpty()) return ItemStack.EMPTY;
        CompoundTag tag = upgrade.getTag();
        if (tag == null || !tag.contains(FILTER_TAG, Tag.TAG_LIST)) return ItemStack.EMPTY;
        ListTag list = tag.getList(FILTER_TAG, Tag.TAG_COMPOUND);
        if (idx >= list.size()) return ItemStack.EMPTY;
        return ItemStack.of(list.getCompound(idx));
    }

    public static int getFilterMode(ItemStack upgrade) {
        if (upgrade.isEmpty()) return 0;
        CompoundTag tag = upgrade.getTag();
        return tag != null ? tag.getInt(MODE_TAG) : 0;
    }
}
