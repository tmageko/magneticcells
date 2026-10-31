package com.magneticcells.compat;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.storage.StorageCells;
import com.magneticcells.items.CellData;
import com.magneticcells.items.MagneticStorageCellItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

@EventBusSubscriber(modid = com.magneticcells.MagneticCellsMod.MODID,
                    bus = EventBusSubscriber.Bus.MOD)
public class AE2Integration {

    public static boolean isLoaded() {
        return ModList.get().isLoaded("ae2");
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!isLoaded()) return;
        event.enqueueWork(() ->
            StorageCells.addCellHandler(new MagneticCellHandler())
        );
    }

    public static class MagneticCellHandler implements ICellHandler {
        @Override
        public boolean isCell(ItemStack is) {
            return is.getItem() instanceof MagneticStorageCellItem;
        }

        @Override
        public StorageCell getCellInventory(ItemStack is, ISaveProvider host) {
            if (!(is.getItem() instanceof MagneticStorageCellItem ci)) return null;
            return new MagneticStorageCell(is, host, ci);
        }
    }

    public static class MagneticStorageCell implements StorageCell {

        private final ItemStack cellStack;
        private final MagneticStorageCellItem cellItem;
        private final ISaveProvider saveProvider;

        public MagneticStorageCell(ItemStack stack, ISaveProvider saveProvider, MagneticStorageCellItem cellItem) {
            this.cellStack    = stack;
            this.cellItem     = cellItem;
            this.saveProvider = saveProvider;
        }

        @Override
        public Component getDescription() { return cellStack.getDisplayName(); }

        @Override
        public CellState getStatus() {
            List<CellData.StoredStack> stored = CellData.getStored(cellStack);
            long total = CellData.getTotalStored(cellStack);
            long max   = cellItem.getMaxItems();
            if (total == 0) return CellState.EMPTY;
            if (stored.size() >= cellItem.getMaxTypes() || total >= max) return CellState.FULL;
            if (total > max * 0.75) return CellState.TYPES_FULL;
            return CellState.NOT_EMPTY;
        }

        @Override
        public double getIdleDrain() { return 0.5; }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof AEItemKey itemKey)) return 0;
            ItemStack item = itemKey.toStack(1);
            if (!CellData.isAllowed(cellStack, item)) return 0;

            List<CellData.StoredStack> stored = CellData.getStored(cellStack);
            long free = cellItem.getMaxItems() - CellData.getTotalStored(cellStack);
            if (free <= 0) return 0;

            boolean exists = stored.stream().anyMatch(s -> ItemStack.isSameItemSameComponents(s.type, item));
            if (!exists && stored.size() >= cellItem.getMaxTypes()) return 0;

            long toInsert = Math.min(amount, free);
            if (mode == Actionable.MODULATE) {
                boolean found = false;
                for (CellData.StoredStack ss : stored) {
                    if (ItemStack.isSameItemSameComponents(ss.type, item)) {
                        ss.amount += toInsert;
                        found = true;
                        break;
                    }
                }
                if (!found) stored.add(new CellData.StoredStack(item.copyWithCount(1), toInsert));
                CellData.setStored(cellStack, stored);
                if (saveProvider != null) saveProvider.saveChanges();
            }
            return toInsert;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof AEItemKey itemKey)) return 0;
            ItemStack item = itemKey.toStack(1);
            List<CellData.StoredStack> stored = CellData.getStored(cellStack);
            for (int i = 0; i < stored.size(); i++) {
                CellData.StoredStack ss = stored.get(i);
                if (ItemStack.isSameItemSameComponents(ss.type, item)) {
                    long can = Math.min(ss.amount, amount);
                    if (mode == Actionable.MODULATE) {
                        ss.amount -= can;
                        if (ss.amount <= 0) stored.remove(i);
                        CellData.setStored(cellStack, stored);
                        if (saveProvider != null) saveProvider.saveChanges();
                    }
                    return can;
                }
            }
            return 0;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (CellData.StoredStack ss : CellData.getStored(cellStack))
                if (!ss.type.isEmpty() && ss.amount > 0)
                    out.add(AEItemKey.of(ss.type), ss.amount);
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            if (!(what instanceof AEItemKey itemKey)) return false;
            return CellData.isAllowed(cellStack, itemKey.toStack(1));
        }

        @Override
        public void persist() {}
    }
}
