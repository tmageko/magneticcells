package com.magneticcells.items;

import com.magneticcells.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CellData {

    public static final int MODE_WHITELIST = 0;
    public static final int MODE_BLACKLIST = 1;

    public static class StoredStack {
        public final ItemStack type;
        public long amount;
        public StoredStack(ItemStack type, long amount) {
            this.type   = type.copyWithCount(1);
            this.amount = amount;
        }
        public ItemStack toDisplayStack() {
            return type.copyWithCount((int) Math.min(amount, type.getItem().getMaxStackSize(type)));
        }
    }

    // ── Геттер/сеттер компонента ──

    private static MagCellComponent get(ItemStack stack) {
        MagCellComponent c = stack.get(ModDataComponents.CELL_DATA.get());
        return c != null ? c : MagCellComponent.EMPTY;
    }

    private static void set(ItemStack stack, MagCellComponent component) {
        stack.set(ModDataComponents.CELL_DATA.get(), component);
    }

    // ── Явная запись в инвентарь (для сохранения) ──

    public static void markDirty(Inventory inv, int slot) {
        ItemStack stack = inv.getItem(slot);
        inv.setItem(slot, stack);
        inv.setChanged();
    }

    // ── Хранилище ──

    public static List<StoredStack> getStored(ItemStack cell) {
        return get(cell).toStoredStacks();
    }

    public static void setStored(ItemStack cell, List<StoredStack> stored) {
        set(cell, get(cell).withStorage(MagCellComponent.fromStoredStacks(stored)));
    }

    public static List<ItemStack> getStoredItems(ItemStack cell) {
        List<ItemStack> result = new ArrayList<>();
        for (StoredStack s : getStored(cell))
            result.add(s.type.copyWithCount((int) Math.min(s.amount, Integer.MAX_VALUE)));
        return result;
    }

    // ── Вставка ──

    public static ItemStack insertItem(ItemStack cell, ItemStack toInsert, int maxItems, int maxTypes) {
        if (toInsert.isEmpty()) return ItemStack.EMPTY;
        List<StoredStack> stored = getStored(cell);
        long totalStored = stored.stream().mapToLong(s -> s.amount).sum();
        long freeSpace   = maxItems - totalStored;
        if (freeSpace <= 0) return toInsert.copy();

        long toAdd    = Math.min(toInsert.getCount(), freeSpace);
        long leftover = toInsert.getCount() - toAdd;

        for (StoredStack s : stored) {
            if (ItemStack.isSameItemSameComponents(s.type, toInsert)) {
                s.amount += toAdd;
                setStored(cell, stored);
                return leftover > 0 ? toInsert.copyWithCount((int) leftover) : ItemStack.EMPTY;
            }
        }
        if (stored.size() >= maxTypes) return toInsert.copy();
        stored.add(new StoredStack(toInsert, toAdd));
        setStored(cell, stored);
        return leftover > 0 ? toInsert.copyWithCount((int) leftover) : ItemStack.EMPTY;
    }

    // ── Фильтр ──

    public static int getFilterMode(ItemStack stack) { return get(stack).filterMode(); }

    public static void setFilterMode(ItemStack stack, int mode) {
        set(stack, get(stack).withFilterMode(mode));
    }

    public static void toggleFilterMode(ItemStack stack) {
        setFilterMode(stack, getFilterMode(stack) == MODE_WHITELIST ? MODE_BLACKLIST : MODE_WHITELIST);
    }

    public static boolean getMagnetEnabled(ItemStack stack) { return get(stack).magnetEnabled(); }

    public static void setMagnetEnabled(ItemStack stack, boolean enabled) {
        set(stack, get(stack).withMagnet(enabled));
    }

    public static void toggleMagnet(ItemStack stack) {
        setMagnetEnabled(stack, !getMagnetEnabled(stack));
    }

    public static List<ItemStack> getFilterItems(ItemStack stack) {
        return get(stack).toFilterStacks();
    }

    public static void setFilterItems(ItemStack stack, List<ItemStack> items) {
        set(stack, get(stack).withFilter(MagCellComponent.filterFromStacks(items)));
    }

    public static boolean isAllowed(ItemStack cell, ItemStack item) {
        List<ItemStack> filterItems = getFilterItems(cell);
        if (filterItems.stream().allMatch(ItemStack::isEmpty)) return true;
        boolean inFilter = filterItems.stream().anyMatch(f -> !f.isEmpty() && ItemStack.isSameItem(f, item));
        return getFilterMode(cell) == MODE_WHITELIST ? inFilter : !inFilter;
    }

    public static long getTotalStored(ItemStack stack) {
        return getStored(stack).stream().mapToLong(s -> s.amount).sum();
    }

    public static boolean isAllowedByTag(ItemStack upgradeItem, ItemStack target,
                                          String filterTag, String modeTag) {
        var customData = upgradeItem.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return true;
        CompoundTag root = customData.copyTag();
        int mode = root.getInt(modeTag);
        if (!root.contains(filterTag, Tag.TAG_LIST)) return true;
        ListTag list = root.getList(filterTag, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return true;
        boolean inFilter = false;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.contains("ItemId")) continue;
            ResourceLocation rl = ResourceLocation.tryParse(entry.getString("ItemId"));
            if (rl == null) continue;
            var item = BuiltInRegistries.ITEM.get(rl);
            if (item != null && ItemStack.isSameItem(new ItemStack(item), target)) {
                inFilter = true; break;
            }
        }
        return mode == MODE_WHITELIST ? inFilter : !inFilter;
    }
}
