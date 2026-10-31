package com.magneticcells.registry;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.gui.MagneticCellMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(Registries.MENU, MagneticCellsMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MagneticCellMenu>> MAGNETIC_CELL_MENU =
        MENU_TYPES.register("magnetic_cell_menu",
            () -> IMenuTypeExtension.create((id, inv, buf) ->
                new MagneticCellMenu(null, id, inv, buf.readInt())));
}
