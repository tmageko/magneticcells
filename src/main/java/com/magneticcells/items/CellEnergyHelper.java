package com.magneticcells.items;

import com.magneticcells.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

public class CellEnergyHelper {

    public static final int MAX_ENERGY     = 100_000;
    public static final int ENERGY_PER_TICK = 10;

    public static int getEnergy(ItemStack stack) {
        MagCellComponent c = stack.get(ModDataComponents.CELL_DATA.get());
        return c != null ? c.energy() : 0;
    }

    public static void setEnergy(ItemStack stack, int energy) {
        int clamped = Math.max(0, Math.min(energy, MAX_ENERGY));
        MagCellComponent c = stack.get(ModDataComponents.CELL_DATA.get());
        stack.set(ModDataComponents.CELL_DATA.get(),
            (c != null ? c : MagCellComponent.EMPTY).withEnergy(clamped));
    }

    public static boolean hasEnergy(ItemStack stack) {
        return getEnergy(stack) >= ENERGY_PER_TICK;
    }

    public static void consumeEnergy(ItemStack stack, int amount) {
        setEnergy(stack, getEnergy(stack) - amount);
    }

    public static int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        int current  = getEnergy(stack);
        int received = Math.min(maxReceive, MAX_ENERGY - current);
        if (!simulate) setEnergy(stack, current + received);
        return received;
    }

    public static int extractEnergy(ItemStack stack, int maxExtract, boolean simulate) {
        int current   = getEnergy(stack);
        int extracted = Math.min(maxExtract, current);
        if (!simulate) setEnergy(stack, current - extracted);
        return extracted;
    }

    public static int getChargePercent(ItemStack stack) {
        return (int)(getEnergy(stack) * 100L / MAX_ENERGY);
    }
}
