package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.gui.MagneticCellMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketVoidFilterAction(int action, int slotIdx, ItemStack stack) implements CustomPacketPayload {

    public static final int SET_SLOT    = 0;
    public static final int TOGGLE_MODE = 1;
    private static final String FILTER_TAG  = "VoidFilter";
    private static final String MODE_TAG    = "VoidMode";
    private static final int    FILTER_SIZE = 9;
    private static final String ITEM_ID_TAG = "ItemId";

    public static final Type<PacketVoidFilterAction> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MagneticCellsMod.MODID, "void_filter_action"));

    // Кодируем ItemStack по ID без HolderLookup
    private static final StreamCodec<FriendlyByteBuf, ItemStack> ITEM_CODEC =
        StreamCodec.of(
            (buf, s) -> buf.writeUtf(s.isEmpty() ? "" :
                BuiltInRegistries.ITEM.getKey(s.getItem()).toString()),
            buf -> {
                String id = buf.readUtf();
                if (id.isEmpty()) return ItemStack.EMPTY;
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if (rl == null) return ItemStack.EMPTY;
                var item = BuiltInRegistries.ITEM.get(rl);
                return item == null ? ItemStack.EMPTY : new ItemStack(item);
            }
        );

    public static final StreamCodec<FriendlyByteBuf, PacketVoidFilterAction> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, PacketVoidFilterAction::action,
            ByteBufCodecs.INT, PacketVoidFilterAction::slotIdx,
            ITEM_CODEC,        PacketVoidFilterAction::stack,
            PacketVoidFilterAction::new
        );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketVoidFilterAction pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (!(player.containerMenu instanceof MagneticCellMenu menu)) return;
            ItemStack upgrade = menu.getUpgradeItem();
            if (upgrade.isEmpty()) return;
            CustomData existingData = upgrade.get(DataComponents.CUSTOM_DATA);
            CompoundTag tag = existingData != null ? existingData.copyTag() : new CompoundTag();

            if (pkt.action == SET_SLOT) {
                ListTag list = tag.contains(FILTER_TAG, Tag.TAG_LIST)
                    ? tag.getList(FILTER_TAG, Tag.TAG_COMPOUND) : new ListTag();
                while (list.size() < FILTER_SIZE) list.add(new CompoundTag());
                if (pkt.slotIdx >= 0 && pkt.slotIdx < FILTER_SIZE) {
                    CompoundTag entry = new CompoundTag();
                    if (!pkt.stack.isEmpty())
                        entry.putString(ITEM_ID_TAG, BuiltInRegistries.ITEM.getKey(pkt.stack.getItem()).toString());
                    list.set(pkt.slotIdx, entry);
                }
                tag.put(FILTER_TAG, list);
                upgrade.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                menu.setUpgradeItem(upgrade);
            } else if (pkt.action == TOGGLE_MODE) {
                tag.putInt(MODE_TAG, tag.getInt(MODE_TAG) == 0 ? 1 : 0);
                upgrade.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                menu.setUpgradeItem(upgrade);
            }
            player.containerMenu.broadcastChanges();
        });
    }

    public static ItemStack getFilterSlot(ItemStack upgrade, int idx) {
        if (upgrade.isEmpty()) return ItemStack.EMPTY;
        CustomData cd = upgrade.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return ItemStack.EMPTY;
        CompoundTag tag = cd.copyTag();
        if (!tag.contains(FILTER_TAG, Tag.TAG_LIST)) return ItemStack.EMPTY;
        ListTag list = tag.getList(FILTER_TAG, Tag.TAG_COMPOUND);
        if (idx >= list.size()) return ItemStack.EMPTY;
        CompoundTag entry = list.getCompound(idx);
        if (!entry.contains(ITEM_ID_TAG)) return ItemStack.EMPTY;
        ResourceLocation rl = ResourceLocation.tryParse(entry.getString(ITEM_ID_TAG));
        if (rl == null) return ItemStack.EMPTY;
        var item = BuiltInRegistries.ITEM.get(rl);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static int getFilterMode(ItemStack upgrade) {
        if (upgrade.isEmpty()) return 0;
        CustomData cd = upgrade.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return 0;
        return cd.copyTag().getInt(MODE_TAG);
    }
}
