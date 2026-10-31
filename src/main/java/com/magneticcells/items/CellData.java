package com.magneticcells.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранение: каждый тип предмета = { id, tag, Count(long) }
 * Count хранится отдельно как LongTag чтобы не ограничиваться 64.
 */
public class CellData {

    public static final String ROOT_TAG         = "MagCell";
    public static final String FILTER_MODE_TAG  = "FilterMode";
    public static final String MAGNET_ENABLED_TAG = "MagnetEnabled";
    public static final String FILTER_ITEMS_TAG = "FilterItems";
    public static final String STORAGE_TAG      = "Storage";
    public static final String AMOUNT_TAG       = "Amount"; // long, отдельно от Count

    public static final int MODE_WHITELIST = 0;
    public static final int MODE_BLACKLIST = 1;

    // ── Внутренний класс для хранения типа + количества ──

    public static class StoredStack {
        public final ItemStack type; // count=1, только тип
        public long amount;

        public StoredStack(ItemStack type, long amount) {
            this.type   = type.copyWithCount(1);
            this.amount = amount;
        }

        /** Возвращает ItemStack для отображения (count ограничен 64) */
        public ItemStack toDisplayStack() {
            return type.copyWithCount((int) Math.min(amount, type.getItem().getMaxStackSize()));
        }
    }

    // ── Чтение/запись хранилища ──

    public static List<StoredStack> getStored(ItemStack cell) {
        List<StoredStack> result = new ArrayList<>();
        CompoundTag root = getRoot(cell);
        if (!root.contains(STORAGE_TAG, Tag.TAG_LIST)) return result;
        ListTag list = root.getList(STORAGE_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ItemStack type = ItemStack.of(entry);
            long amount = entry.contains(AMOUNT_TAG) ? entry.getLong(AMOUNT_TAG) : type.getCount();
            if (!type.isEmpty() && amount > 0)
                result.add(new StoredStack(type, amount));
        }
        return result;
    }

    public static void setStored(ItemStack cell, List<StoredStack> stored) {
        ListTag list = new ListTag();
        for (StoredStack s : stored) {
            if (s.amount <= 0) continue;
            CompoundTag entry = new CompoundTag();
            s.type.copyWithCount(1).save(entry); // сохраняем тип (count=1)
            entry.putLong(AMOUNT_TAG, s.amount);  // количество отдельно
            list.add(entry);
        }
        getOrCreateRoot(cell).put(STORAGE_TAG, list);
    }

    // Совместимость — возвращает List<ItemStack> для старого кода
    public static List<ItemStack> getStoredItems(ItemStack cell) {
        List<ItemStack> result = new ArrayList<>();
        for (StoredStack s : getStored(cell))
            result.add(s.type.copyWithCount((int) Math.min(s.amount, Integer.MAX_VALUE)));
        return result;
    }

    public static void setStoredItems(ItemStack cell, List<ItemStack> items) {
        List<StoredStack> stored = new ArrayList<>();
        for (ItemStack item : items) {
            if (!item.isEmpty())
                stored.add(new StoredStack(item, item.getCount()));
        }
        setStored(cell, stored);
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

        // Ищем существующий тип
        for (StoredStack s : stored) {
            if (ItemStack.isSameItemSameTags(s.type, toInsert)) {
                s.amount += toAdd;
                setStored(cell, stored);
                return leftover > 0 ? toInsert.copyWithCount((int) leftover) : ItemStack.EMPTY;
            }
        }

        // Новый тип
        if (stored.size() >= maxTypes)
            return toInsert.copy();

        stored.add(new StoredStack(toInsert, toAdd));
        setStored(cell, stored);
        return leftover > 0 ? toInsert.copyWithCount((int) leftover) : ItemStack.EMPTY;
    }

    // ── Фильтр ──

    public static int getFilterMode(ItemStack stack) { return getRoot(stack).getInt(FILTER_MODE_TAG); }
    public static void setFilterMode(ItemStack stack, int mode) { getOrCreateRoot(stack).putInt(FILTER_MODE_TAG, mode); }
    public static void toggleFilterMode(ItemStack stack) {
        setFilterMode(stack, getFilterMode(stack) == MODE_WHITELIST ? MODE_BLACKLIST : MODE_WHITELIST);
    }

    // Магнит вкл/выкл (по умолчанию включён)
    public static boolean getMagnetEnabled(ItemStack stack) {
        if (stack.isEmpty() || stack.getTag() == null) return true;
        if (!getRoot(stack).contains(MAGNET_ENABLED_TAG)) return true; // default ON
        return getRoot(stack).getBoolean(MAGNET_ENABLED_TAG);
    }
    public static void setMagnetEnabled(ItemStack stack, boolean enabled) {
        getOrCreateRoot(stack).putBoolean(MAGNET_ENABLED_TAG, enabled);
    }
    public static void toggleMagnet(ItemStack stack) {
        setMagnetEnabled(stack, !getMagnetEnabled(stack));
    }

    public static List<ItemStack> getFilterItems(ItemStack stack) {
        List<ItemStack> result = new ArrayList<>();
        CompoundTag root = getRoot(stack);
        if (root.contains(FILTER_ITEMS_TAG, Tag.TAG_LIST)) {
            ListTag list = root.getList(FILTER_ITEMS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) result.add(ItemStack.of(list.getCompound(i)));
        }
        return result;
    }

    public static void setFilterItems(ItemStack stack, List<ItemStack> items) {
        ListTag list = new ListTag();
        for (ItemStack item : items) {
            if (!item.isEmpty()) { CompoundTag tag = new CompoundTag(); item.save(tag); list.add(tag); }
        }
        getOrCreateRoot(stack).put(FILTER_ITEMS_TAG, list);
    }

    public static boolean isAllowed(ItemStack cell, ItemStack item) {
        List<ItemStack> filterItems = getFilterItems(cell);
        if (filterItems.isEmpty()) return true;
        boolean inFilter = filterItems.stream().anyMatch(f -> !f.isEmpty() && ItemStack.isSameItem(f, item));
        return getFilterMode(cell) == MODE_WHITELIST ? inFilter : !inFilter;
    }

    public static boolean isAllowedByTag(ItemStack item, ItemStack target, String filterTag, String modeTag) {
        CompoundTag root = item.getTag();
        if (root == null) return true;
        int mode = root.getInt(modeTag);
        if (!root.contains(filterTag, Tag.TAG_LIST)) return true;
        ListTag list = root.getList(filterTag, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return true;
        boolean inFilter = false;
        for (int i = 0; i < list.size(); i++) {
            ItemStack f = ItemStack.of(list.getCompound(i));
            if (!f.isEmpty() && ItemStack.isSameItem(f, target)) { inFilter = true; break; }
        }
        return mode == MODE_WHITELIST ? inFilter : !inFilter;
    }

    public static long getTotalStored(ItemStack stack) {
        return getStored(stack).stream().mapToLong(s -> s.amount).sum();
    }

    // ── Internal ──

    private static CompoundTag getRoot(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(ROOT_TAG, Tag.TAG_COMPOUND)) return nbt.getCompound(ROOT_TAG);
        return new CompoundTag();
    }
    private static CompoundTag getOrCreateRoot(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(ROOT_TAG, Tag.TAG_COMPOUND)) nbt.put(ROOT_TAG, new CompoundTag());
        return nbt.getCompound(ROOT_TAG);
    }
}
