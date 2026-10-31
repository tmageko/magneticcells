package com.magneticcells;

import com.magneticcells.network.PacketHandler;
import com.magneticcells.registry.ModCreativeTab;
import com.magneticcells.registry.ModItems;
import com.magneticcells.registry.ModMenuTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MagneticCellsMod.MODID)
public class MagneticCellsMod {

    public static final String MODID = "magneticcells";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public MagneticCellsMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(bus);
        ModMenuTypes.MENU_TYPES.register(bus);
        ModCreativeTab.CREATIVE_TABS.register(bus);

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
            LOGGER.info("Magnetic Cells: common setup done");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            com.magneticcells.gui.ModScreens.register();
            LOGGER.info("Magnetic Cells: client setup done");
        });
    }
}
