package com.magneticcells.capability;

import com.magneticcells.items.CellData;
import com.magneticcells.items.CellEnergyHelper;
import com.magneticcells.items.MagneticStorageCellItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = com.magneticcells.MagneticCellsMod.MODID)
public class MagnetEventHandler {

    private static final double MAGNET_RADIUS  = 8.0;
    private static final int    CHECK_INTERVAL = 5;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % CHECK_INTERVAL != 0) return;

        int cellSlot = findMagneticCellSlot(player);
        if (cellSlot < 0) return;
        ItemStack cellStack = player.getInventory().getItem(cellSlot);

        if (!CellEnergyHelper.hasEnergy(cellStack)) return;
        if (!CellData.getMagnetEnabled(cellStack)) return;

        MagneticStorageCellItem cellItem = (MagneticStorageCellItem) cellStack.getItem();

        AABB area = player.getBoundingBox().inflate(MAGNET_RADIUS);
        List<ItemEntity> nearby = player.level().getEntitiesOfClass(
            ItemEntity.class, area,
            e -> !e.hasPickUpDelay() && e.isAlive()
        );

        boolean absorbed = false;

        for (ItemEntity itemEntity : nearby) {
            ItemStack dropped = itemEntity.getItem();
            if (dropped.isEmpty()) continue;
            if (!CellData.isAllowed(cellStack, dropped)) continue;

            // Используем StoredStack напрямую — без потери количества
            List<CellData.StoredStack> stored = CellData.getStored(cellStack);
            long totalStored = stored.stream().mapToLong(s -> s.amount).sum();
            long freeSpace = cellItem.getMaxItems() - totalStored;
            if (freeSpace <= 0) break;

            long toAdd = Math.min(dropped.getCount(), freeSpace);

            boolean found = false;
            for (CellData.StoredStack ss : stored) {
                if (ItemStack.isSameItemSameComponents(ss.type, dropped)) {
                    ss.amount += toAdd;
                    found = true;
                    break;
                }
            }

            if (!found) {
                if (stored.size() >= cellItem.getMaxTypes()) continue;
                stored.add(new CellData.StoredStack(dropped.copyWithCount(1), toAdd));
            }

            CellData.setStored(cellStack, stored);

            int leftover = (int)(dropped.getCount() - toAdd);
            if (leftover <= 0) itemEntity.discard();
            else itemEntity.setItem(dropped.copyWithCount(leftover));

            absorbed = true;
        }

        if (absorbed) {
            CellEnergyHelper.consumeEnergy(cellStack, CellEnergyHelper.ENERGY_PER_TICK);
            CellData.markDirty(player.getInventory(), cellSlot);
        }
    }

    private static int findMagneticCellSlot(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < 9; i++)
            if (inv.getItem(i).getItem() instanceof MagneticStorageCellItem) return i;
        for (int i = 9; i < 36; i++)
            if (inv.getItem(i).getItem() instanceof MagneticStorageCellItem) return i;
        // offhand не имеет слота в обычном инвентаре — пропускаем
        return -1;
    }

    private static ItemStack findMagneticCell(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof MagneticStorageCellItem) return s;
        }
        for (int i = 9; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof MagneticStorageCellItem) return s;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof MagneticStorageCellItem) return off;
        return ItemStack.EMPTY;
    }
}
