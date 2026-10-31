package com.magneticcells.registry;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.items.CellEnergyHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MagneticCellsMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAGNETIC_CELLS_TAB =
        CREATIVE_TABS.register("magnetic_cells_tab", () ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.magneticcells.magnetic_cells_tab"))
                .icon(() -> new ItemStack(ModItems.MAGNETIC_CELL_1K.get()))
                .displayItems((params, output) -> {
                    output.accept(ModItems.MAGNETIC_CELL_HOUSING.get());
                    // Разряженные ячейки
                    output.accept(ModItems.MAGNETIC_CELL_1K.get());
                    output.accept(ModItems.MAGNETIC_CELL_4K.get());
                    output.accept(ModItems.MAGNETIC_CELL_16K.get());
                    output.accept(ModItems.MAGNETIC_CELL_64K.get());
                    output.accept(ModItems.MAGNETIC_CELL_256K.get());
                    output.accept(ModItems.MAGNETIC_CELL_1M.get());
                    output.accept(ModItems.MAGNETIC_CELL_4M.get());
                    output.accept(ModItems.MAGNETIC_CELL_16M.get());
                    output.accept(ModItems.MAGNETIC_CELL_64M.get());
                    output.accept(ModItems.MAGNETIC_CELL_256M.get());
                    // Заряженные ячейки
                    for (var reg : java.util.List.of(
                            ModItems.MAGNETIC_CELL_1K, ModItems.MAGNETIC_CELL_4K,
                            ModItems.MAGNETIC_CELL_16K, ModItems.MAGNETIC_CELL_64K,
                            ModItems.MAGNETIC_CELL_256K, ModItems.MAGNETIC_CELL_1M,
                            ModItems.MAGNETIC_CELL_4M, ModItems.MAGNETIC_CELL_16M,
                            ModItems.MAGNETIC_CELL_64M, ModItems.MAGNETIC_CELL_256M)) {
                        ItemStack charged = new ItemStack(reg.get());
                        CellEnergyHelper.setEnergy(charged, CellEnergyHelper.MAX_ENERGY);
                        output.accept(charged);
                    }
                })
                .build()
        );
}