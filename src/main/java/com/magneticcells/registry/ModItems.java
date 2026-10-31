package com.magneticcells.registry;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.items.MagneticStorageCellItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

@EventBusSubscriber(modid = MagneticCellsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, MagneticCellsMod.MODID);

    public static final DeferredHolder<Item, Item> MAGNETIC_CELL_HOUSING =
        ITEMS.register("magnetic_cell_housing", () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_1K =
        ITEMS.register("magnetic_cell_1k",   () -> new MagneticStorageCellItem(1_024,       63, "1k"));
    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_4K =
        ITEMS.register("magnetic_cell_4k",   () -> new MagneticStorageCellItem(4_096,       63, "4k"));
    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_16K =
        ITEMS.register("magnetic_cell_16k",  () -> new MagneticStorageCellItem(16_384,      63, "16k"));
    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_64K =
        ITEMS.register("magnetic_cell_64k",  () -> new MagneticStorageCellItem(65_536,      63, "64k"));
    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_256K =
        ITEMS.register("magnetic_cell_256k", () -> new MagneticStorageCellItem(262_144,     63, "256k"));
    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_1M =
        ITEMS.register("magnetic_cell_1m",   () -> new MagneticStorageCellItem(1_048_576,   63, "1m"));
    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_4M =
        ITEMS.register("magnetic_cell_4m",   () -> new MagneticStorageCellItem(4_194_304,   63, "4m"));
    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_16M =
        ITEMS.register("magnetic_cell_16m",  () -> new MagneticStorageCellItem(16_777_216,  63, "16m"));
    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_64M =
        ITEMS.register("magnetic_cell_64m",  () -> new MagneticStorageCellItem(67_108_864,  63, "64m"));
    public static final DeferredHolder<Item, MagneticStorageCellItem> MAGNETIC_CELL_256M =
        ITEMS.register("magnetic_cell_256m", () -> new MagneticStorageCellItem(268_435_456, 63, "256m"));

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var reg : List.of(
                MAGNETIC_CELL_1K, MAGNETIC_CELL_4K, MAGNETIC_CELL_16K,
                MAGNETIC_CELL_64K, MAGNETIC_CELL_256K, MAGNETIC_CELL_1M,
                MAGNETIC_CELL_4M, MAGNETIC_CELL_16M, MAGNETIC_CELL_64M,
                MAGNETIC_CELL_256M)) {
            MagneticStorageCellItem.registerCapabilities(event, reg.get());
        }
    }
}
