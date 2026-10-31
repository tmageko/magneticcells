package com.magneticcells.items;

import net.minecraft.world.item.ItemStack;

/**
 * Хелпер для хранения энергии FE прямо в NBT ячейки.
 * Любое устройство с поддержкой Forge Energy сможет её заряжать.
 */
public class CellEnergyHelper {

    public static final String ENERGY_TAG = "Energy";
    public static final int MAX_ENERGY    = 100_000; // 100k FE
    public static final int ENERGY_PER_TICK = 10;    // расход на тик при работе магнита

    public static int getEnergy(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(ENERGY_TAG)) {
            return stack.getTag().getInt(ENERGY_TAG);
        }
        return 0;
    }

    public static void setEnergy(ItemStack stack, int energy) {
        stack.getOrCreateTag().putInt(ENERGY_TAG, Math.max(0, Math.min(energy, MAX_ENERGY)));
    }

    public static boolean hasEnergy(ItemStack stack) {
        return getEnergy(stack) >= ENERGY_PER_TICK;
    }

    public static void consumeEnergy(ItemStack stack, int amount) {
        int current = getEnergy(stack);
        setEnergy(stack, current - amount);
    }

    public static int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        int current = getEnergy(stack);
        int received = Math.min(maxReceive, MAX_ENERGY - current);
        if (!simulate) setEnergy(stack, current + received);
        return received;
    }

    public static int extractEnergy(ItemStack stack, int maxExtract, boolean simulate) {
        int current = getEnergy(stack);
        int extracted = Math.min(maxExtract, current);
        if (!simulate) setEnergy(stack, current - extracted);
        return extracted;
    }

    /** Процент заряда 0-100 */
    public static int getChargePercent(ItemStack stack) {
        return (int)(getEnergy(stack) * 100L / MAX_ENERGY);
    }
}
