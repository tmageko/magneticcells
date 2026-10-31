package com.magneticcells.registry;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.gui.MagneticCellMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, MagneticCellsMod.MODID);

    public static final RegistryObject<MenuType<MagneticCellMenu>> MAGNETIC_CELL_MENU =
        MENU_TYPES.register("magnetic_cell_menu",
                () -> IForgeMenuType.create((windowId, inv, buf) -> new MagneticCellMenu(ModMenuTypes.MAGNETIC_CELL_MENU.get(), windowId, inv, buf)));
}
