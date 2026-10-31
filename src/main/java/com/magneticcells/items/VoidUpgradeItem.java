package com.magneticcells.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class VoidUpgradeItem extends Item {

    public static final String VOID_MODE_TAG    = "VoidMode";    // 0=whitelist,1=blacklist
    public static final String VOID_FILTER_TAG  = "VoidFilter";  // список предметов

    public VoidUpgradeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static boolean shouldVoid(ItemStack upgrade, ItemStack item) {
        if (upgrade.isEmpty() || !(upgrade.getItem() instanceof VoidUpgradeItem)) return false;
        return CellData.isAllowedByTag(upgrade, item,
            VOID_FILTER_TAG, VOID_MODE_TAG);
    }
}
