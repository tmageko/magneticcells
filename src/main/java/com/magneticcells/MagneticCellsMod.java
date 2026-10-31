package com.magneticcells;

import com.magneticcells.network.PacketHandler;
import com.magneticcells.registry.ModCreativeTab;
import com.magneticcells.registry.ModDataComponents;
import com.magneticcells.registry.ModItems;
import com.magneticcells.registry.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MagneticCellsMod.MODID)
public class MagneticCellsMod {

    public static final String MODID = "magneticcells";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public MagneticCellsMod(IEventBus bus) {
        ModItems.ITEMS.register(bus);
        ModMenuTypes.MENU_TYPES.register(bus);
        ModCreativeTab.CREATIVE_TABS.register(bus);
        ModDataComponents.DATA_COMPONENTS.register(bus); // регистрируем кастомный компонент

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::registerPackets);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> LOGGER.info("Magnetic Cells: common setup done"));
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> LOGGER.info("Magnetic Cells: client setup done"));
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        PacketHandler.register(event);
    }
}
