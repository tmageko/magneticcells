package com.magneticcells.gui;

import com.magneticcells.items.CellData;
import com.magneticcells.items.CellEnergyHelper;
import com.magneticcells.items.MagneticStorageCellItem;
import com.magneticcells.network.PacketHandler;
import com.magneticcells.network.PacketSyncContents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class MagneticCellMenu extends AbstractContainerMenu {

    public static final int FILTER_SLOTS = 9;
    public static final int CONTENT_COLS = 9;
    public static final int CONTENT_ROWS = 5;

    public static final int GUI_W     = 176;
    public static final int GUI_H     = 202;
    public static final int FILTER_X  = 4;
    public static final int FILTER_Y  = 20;
    public static final int CONTENT_X = 4;
    public static final int CONTENT_Y = 56;
    public static final int INV_X     = 4;
    public static final int INV_Y     = 122;
    public static final int HOTBAR_Y  = 180;

    public final int cellSlotIndex;
    public final Inventory playerInventory;
    public int clientEnergy    = 0;
    public int clientMaxEnergy = com.magneticcells.items.CellEnergyHelper.MAX_ENERGY;

    private final ItemStack[] filterSlots = new ItemStack[FILTER_SLOTS];

    public MagneticCellMenu(MenuType<?> type, int windowId, Inventory inv, int cellSlotIndex) {
        super(type, windowId);
        this.playerInventory = inv;
        this.cellSlotIndex   = cellSlotIndex;

        for (int i = 0; i < FILTER_SLOTS; i++) filterSlots[i] = ItemStack.EMPTY;

        loadFilterFromCell();

        // Слоты фильтра
        for (int i = 0; i < FILTER_SLOTS; i++) {
            final int idx = i;
            addSlot(new FilterSlot(this, idx, FILTER_X + i * 18, FILTER_Y));
        }

        // Слоты контента (фиктивные)
        for (int row = 0; row < CONTENT_ROWS; row++)
            for (int col = 0; col < CONTENT_COLS; col++)
                addSlot(new ContentSlot(this, row * CONTENT_COLS + col,
                    CONTENT_X + col * 18, CONTENT_Y + row * 18));

        // Инвентарь
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, INV_X + col * 18, INV_Y + row * 18));

        // Хотбар
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(inv, col, INV_X + col * 18, HOTBAR_Y));
    }

    public MagneticCellMenu(MenuType<?> type, int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(type, windowId, inv, buf.readInt());
    }

    // ── Фильтр ──

    private void loadFilterFromCell() {
        ItemStack cell = getCellStack();
        if (cell.isEmpty()) return;
        List<ItemStack> items = CellData.getFilterItems(cell);
        for (int i = 0; i < Math.min(items.size(), FILTER_SLOTS); i++)
            filterSlots[i] = items.get(i).copy();
    }

    public ItemStack getFilterSlot(int i) {
        return (i >= 0 && i < FILTER_SLOTS) ? filterSlots[i] : ItemStack.EMPTY;
    }

    public void setFilterSlot(int i, ItemStack stack) {
        if (i < 0 || i >= FILTER_SLOTS) return;
        filterSlots[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        saveFilterToCell();
    }

    private void saveFilterToCell() {
        ItemStack cell = getCellStack();
        if (cell.isEmpty()) return;
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack fs : filterSlots) items.add(fs.copy());
        CellData.setFilterItems(cell, items);
    }

    // ── Upgrade slot ──
    private ItemStack upgradeItem = ItemStack.EMPTY;

    public ItemStack getUpgradeItem() { return upgradeItem; }

    public void setUpgradeItem(ItemStack stack) {
        this.upgradeItem = stack == null ? ItemStack.EMPTY : stack;
        saveFilterToCell();
    }

    public int getFilterMode() { return CellData.getFilterMode(getCellStack()); }

    public void toggleFilterMode() {
        ItemStack cell = getCellStack();
        if (!cell.isEmpty()) CellData.toggleFilterMode(cell);
    }

    // ── Синхронизация ──

    public void syncToClient(Player player) {
        if (!(player instanceof ServerPlayer sp)) return;
        int energy = CellEnergyHelper.getEnergy(getCellStack());
        PacketHandler.CHANNEL.send(
            PacketDistributor.PLAYER.with(() -> sp),
            new PacketSyncContents(new ItemStack[0], energy, CellEnergyHelper.MAX_ENERGY)
        );
    }

    public void loadContentFromCell() {}

    // ── Извлечение ──

    public void extractOne(int idx, Player player) {
        ItemStack cell = getCellStack();
        if (cell.isEmpty()) return;
        List<CellData.StoredStack> stored = new ArrayList<>(CellData.getStored(cell));
        if (idx < 0 || idx >= stored.size()) return;
        CellData.StoredStack ss = stored.get(idx);
        ItemStack give = ss.type.copyWithCount(1);
        if (player.getInventory().add(give)) {
            ss.amount--;
            if (ss.amount <= 0) stored.remove(idx);
            CellData.setStored(cell, stored);
            syncToClient(player);
            player.containerMenu.broadcastChanges();
        }
    }

    public void extractAll(int idx, Player player) {
        ItemStack cell = getCellStack();
        if (cell.isEmpty()) return;
        List<CellData.StoredStack> stored = new ArrayList<>(CellData.getStored(cell));
        if (idx < 0 || idx >= stored.size()) return;
        CellData.StoredStack ss = stored.get(idx);
        int maxStack = ss.type.getItem().getMaxStackSize();
        long remaining = ss.amount;
        long extracted = 0;
        while (remaining > 0) {
            int chunk = (int) Math.min(maxStack, remaining);
            ItemStack give = ss.type.copyWithCount(chunk);
            if (!player.getInventory().add(give)) break;
            long added = chunk - give.getCount();
            if (added <= 0) break;
            extracted += added;
            remaining -= added;
        }
        if (extracted > 0) {
            ss.amount -= extracted;
            if (ss.amount <= 0) stored.remove(idx);
            CellData.setStored(cell, stored);
            syncToClient(player);
            player.containerMenu.broadcastChanges();
        }
    }

    // ── Геттеры ──

    public ItemStack getCellStack() {
        if (cellSlotIndex < 0 || cellSlotIndex >= playerInventory.getContainerSize())
            return ItemStack.EMPTY;
        return playerInventory.getItem(cellSlotIndex);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        ItemStack cell = getCellStack();
        return !cell.isEmpty() && cell.getItem() instanceof MagneticStorageCellItem;
    }

    // ── Слоты ──

    private static final net.minecraft.world.SimpleContainer FAKE_INV =
        new net.minecraft.world.SimpleContainer(10);

    public static class FilterSlot extends Slot {
        private final MagneticCellMenu menu;
        private final int fi;
        public FilterSlot(MagneticCellMenu m, int i, int x, int y) {
            super(FAKE_INV, i, x, y); menu = m; fi = i;
        }
        @Override public ItemStack getItem()           { return menu.getFilterSlot(fi); }
        @Override public void set(ItemStack s)         { menu.setFilterSlot(fi, s); }
        @Override public boolean mayPlace(ItemStack s) { return true; }
        @Override public int getMaxStackSize()         { return 1; }
        @Override public boolean hasItem()             { return !menu.getFilterSlot(fi).isEmpty(); }
        @Override public boolean isActive()            { return false; }
        @Override public ItemStack remove(int amount) {
            ItemStack cur = menu.getFilterSlot(fi);
            if (cur.isEmpty()) return ItemStack.EMPTY;
            menu.setFilterSlot(fi, ItemStack.EMPTY);
            return cur;
        }
    }

    public static class ContentSlot extends Slot {
        public final int ci;
        public ContentSlot(MagneticCellMenu m, int i, int x, int y) {
            super(FAKE_INV, i, x, y); ci = i;
        }
        @Override public ItemStack getItem()           { return ItemStack.EMPTY; }
        @Override public void set(ItemStack s)         {}
        @Override public boolean mayPlace(ItemStack s) { return false; }
        @Override public boolean hasItem()             { return false; }
        @Override public ItemStack remove(int amount)  { return ItemStack.EMPTY; }
        @Override public boolean isActive()            { return false; }
    }
}
