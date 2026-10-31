package com.magneticcells.items;

import com.magneticcells.capability.CellEnergyCapability;
import com.magneticcells.gui.MagneticCellMenu;
import com.magneticcells.registry.ModMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MagneticStorageCellItem extends Item {

    private final int maxItems;
    private final int maxTypes;
    private final String tier;

    public MagneticStorageCellItem(int maxItems, int maxTypes, String tier) {
        super(new Item.Properties().stacksTo(1).fireResistant());
        this.maxItems = maxItems;
        this.maxTypes = maxTypes;
        this.tier     = tier;
    }

    public int getMaxItems()  { return maxItems; }
    public int getMaxTypes()  { return maxTypes; }
    public String getTier()   { return tier; }

    // Регистрация capability вызывается из ModItems
    public static void registerCapabilities(RegisterCapabilitiesEvent event, MagneticStorageCellItem item) {
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> new CellEnergyCapability(stack),
            item
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            final int slot = findCellSlot(player, stack);
            sp.openMenu(new MenuProvider() {
                @Override public Component getDisplayName() {
                    return Component.literal("Magnetic Cell (" + tier.toUpperCase() + ")");
                }
                @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    return new MagneticCellMenu(ModMenuTypes.MAGNETIC_CELL_MENU.get(), id, inv, slot);
                }
            }, buf -> buf.writeInt(slot));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext ctx,
                                List<Component> tooltip, TooltipFlag flag) {
        long stored  = CellData.getTotalStored(stack);
        int types    = CellData.getStoredItems(stack).size();
        int energy   = CellEnergyHelper.getEnergy(stack);
        int percent  = CellEnergyHelper.getChargePercent(stack);

        tooltip.add(Component.literal("§7Тир: §f" + tier.toUpperCase()));
        tooltip.add(Component.literal("§7Предметов: §f" + fmtNum(stored) + " / " + fmtNum(maxItems)));
        tooltip.add(Component.literal("§7Типов: §f" + types + " / " + maxTypes));

        String ec = percent > 20 ? "§a" : "§c";
        tooltip.add(Component.literal("§7Энергия: " + ec + energy + " / " + CellEnergyHelper.MAX_ENERGY + " FE §7(" + percent + "%)"));

        int filterMode = CellData.getFilterMode(stack);
        boolean magnetOn = CellData.getMagnetEnabled(stack);
        tooltip.add(Component.literal("§7Фильтр: §f" + (filterMode == CellData.MODE_WHITELIST ? "Белый список" : "Чёрный список")));
        tooltip.add(Component.literal("§7Магнит: " + (magnetOn ? "§aвкл" : "§cвыкл")));

        if (!CellEnergyHelper.hasEnergy(stack)) {
            tooltip.add(Component.literal("§c⚠ Нет энергии — магнит неактивен!"));
        }
    }

    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) {
        return Math.round(CellEnergyHelper.getEnergy(stack) * 13f / CellEnergyHelper.MAX_ENERGY);
    }
    @Override public int getBarColor(ItemStack stack) {
        int p = CellEnergyHelper.getChargePercent(stack);
        return p > 50 ? 0x00FF00 : p > 20 ? 0xFFAA00 : 0xFF0000;
    }

    private static String fmtNum(long n) {
        if (n >= 1_000_000_000L) return String.format("%.1fG", n / 1_000_000_000.0);
        if (n >= 1_000_000L)     return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000L)         return String.format("%.1fK", n / 1_000.0);
        return String.valueOf(n);
    }

    private int findCellSlot(Player player, ItemStack stack) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i) == stack) return i;
        }
        return player.getInventory().selected;
    }
}
