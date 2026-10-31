package com.magneticcells.capability;

import com.magneticcells.items.CellData;
import com.magneticcells.items.CellEnergyHelper;
import com.magneticcells.items.MagneticStorageCellItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = com.magneticcells.MagneticCellsMod.MODID)
public class MagnetEventHandler {

    private static final double MAGNET_RADIUS  = 8.0;
    private static final int    CHECK_INTERVAL = 5;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        if (player.tickCount % CHECK_INTERVAL != 0) return;

        ItemStack cellStack = findMagneticCell(player);
        if (cellStack.isEmpty()) return;
        if (!CellEnergyHelper.hasEnergy(cellStack)) return;
        if (!CellData.getMagnetEnabled(cellStack)) return;

        MagneticStorageCellItem cellItem = (MagneticStorageCellItem) cellStack.getItem();

        AABB area = player.getBoundingBox().inflate(MAGNET_RADIUS);
        List<ItemEntity> nearby = player.level().getEntitiesOfClass(
            ItemEntity.class, area,
            e -> !e.hasPickUpDelay() && e.isAlive()
        );

        // Читаем текущее хранилище ОДИН РАЗ
        List<ItemStack> stored = new ArrayList<>(CellData.getStoredItems(cellStack));
        int totalStored = stored.stream().mapToInt(ItemStack::getCount).sum();
        int maxItems = cellItem.getMaxItems();
        int maxTypes = cellItem.getMaxTypes();

        boolean absorbed = false;

        for (ItemEntity itemEntity : nearby) {
            ItemStack dropped = itemEntity.getItem();
            if (dropped.isEmpty()) continue;
            if (!CellData.isAllowed(cellStack, dropped)) continue;

            int freeSpace = maxItems - totalStored;
            if (freeSpace <= 0) break; // ячейка полна

            int toAdd = Math.min(dropped.getCount(), freeSpace);

            // Ищем существующий тип в нашем локальном списке
            boolean found = false;
            for (ItemStack existing : stored) {
                if (ItemStack.isSameItemSameTags(existing, dropped)) {
                    existing.grow(toAdd);
                    totalStored += toAdd;
                    found = true;
                    break;
                }
            }

            // Новый тип
            if (!found) {
                if (stored.size() >= maxTypes) continue; // нет места для нового типа
                stored.add(dropped.copyWithCount(toAdd));
                totalStored += toAdd;
            }

            // Обновляем entity
            int leftover = dropped.getCount() - toAdd;
            if (leftover <= 0) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(dropped.copyWithCount(leftover));
            }

            absorbed = true;
        }

        // Сохраняем всё одним вызовом в конце тика
        if (absorbed) {
            CellData.setStoredItems(cellStack, stored);
            CellEnergyHelper.consumeEnergy(cellStack, CellEnergyHelper.ENERGY_PER_TICK);
        }
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
