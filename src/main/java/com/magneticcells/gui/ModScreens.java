package com.magneticcells.gui;

import com.magneticcells.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;

public class ModScreens {

    public static void register() {
        MenuScreens.register(ModMenuTypes.MAGNETIC_CELL_MENU.get(), MagneticCellScreen::new);
    }
}
