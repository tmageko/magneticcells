package com.magneticcells.capability;

import com.magneticcells.items.CellEnergyHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Forge Energy capability для ячейки.
 * Благодаря этому любой зарядник с FE поддержкой заряжает ячейку.
 */
public class CellEnergyCapability implements IEnergyStorage {

    private final ItemStack stack;

    public CellEnergyCapability(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return CellEnergyHelper.receiveEnergy(stack, maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        // Не позволяем вытягивать энергию из ячейки
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return CellEnergyHelper.getEnergy(stack);
    }

    @Override
    public int getMaxEnergyStored() {
        return CellEnergyHelper.MAX_ENERGY;
    }

    @Override
    public boolean canExtract() { return false; }

    @Override
    public boolean canReceive() { return true; }
}
