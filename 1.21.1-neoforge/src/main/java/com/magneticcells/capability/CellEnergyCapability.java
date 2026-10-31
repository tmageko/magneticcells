package com.magneticcells.capability;

import com.magneticcells.items.CellEnergyHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CellEnergyCapability implements IEnergyStorage {

    private final ItemStack stack;

    public CellEnergyCapability(ItemStack stack) {
        this.stack = stack;
    }

    @Override public int receiveEnergy(int maxReceive, boolean simulate) {
        return CellEnergyHelper.receiveEnergy(stack, maxReceive, simulate);
    }
    @Override public int extractEnergy(int maxExtract, boolean simulate) {
        return CellEnergyHelper.extractEnergy(stack, maxExtract, simulate);
    }
    @Override public int getEnergyStored() { return CellEnergyHelper.getEnergy(stack); }
    @Override public int getMaxEnergyStored() { return CellEnergyHelper.MAX_ENERGY; }
    @Override public boolean canExtract() { return true; }
    @Override public boolean canReceive() { return true; }
}
