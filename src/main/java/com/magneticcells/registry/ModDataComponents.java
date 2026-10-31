package com.magneticcells.registry;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.items.MagCellComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MagneticCellsMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MagCellComponent>> CELL_DATA =
        DATA_COMPONENTS.register("cell_data", () ->
            DataComponentType.<MagCellComponent>builder()
                .persistent(MagCellComponent.CODEC)
                .networkSynchronized(MagCellComponent.STREAM_CODEC)
                .build()
        );
}
