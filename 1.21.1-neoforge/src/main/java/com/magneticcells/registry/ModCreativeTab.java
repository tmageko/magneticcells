package com.magneticcells.registry;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.items.CellEnergyHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MagneticCellsMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAGNETIC_CELLS_TAB =
        CREATIVE_TABS.register("magnetic_cells_tab", () ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.magneticcells"))
                .icon(() -> new ItemStack(ModItems.MAGNETIC_CELL_1K.get()))
                .displayItems((params, output) -> {
                    output.accept(ModItems.MAGNETIC_CELL_HOUSING.get());
                    for (var reg : List.of(
                            ModItems.MAGNETIC_CELL_1K, ModItems.MAGNETIC_CELL_4K,
                            ModItems.MAGNETIC_CELL_16K, ModItems.MAGNETIC_CELL_64K,
                            ModItems.MAGNETIC_CELL_256K, ModItems.MAGNETIC_CELL_1M,
                            ModItems.MAGNETIC_CELL_4M, ModItems.MAGNETIC_CELL_16M,
                            ModItems.MAGNETIC_CELL_64M, ModItems.MAGNETIC_CELL_256M)) {
                        output.accept(reg.get());
                        ItemStack charged = new ItemStack(reg.get());
                        CellEnergyHelper.setEnergy(charged, CellEnergyHelper.MAX_ENERGY);
                        output.accept(charged);
                    }
                })
                .build());
}
