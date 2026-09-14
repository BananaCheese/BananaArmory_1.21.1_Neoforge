package net.bananacheese.bananasarmory;

import net.bananacheese.bananasarmory.block.BABlocks;
import net.bananacheese.bananasarmory.block.entity.BABlockEntities;
import net.bananacheese.bananasarmory.datagen.BAItemModelProvider;
import net.bananacheese.bananasarmory.item.BAItemGroup;
import net.bananacheese.bananasarmory.item.BAItems;
import net.bananacheese.bananasarmory.screen.BAScreenHandlers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(BananasArmory.MODID)
public class BananasArmory {
    public static final String MODID = "barmory";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BananasArmory(IEventBus modEventBus, ModContainer modContainer) {

        BAItemGroup.register(modEventBus);

        BAItems.ITEMS.register(modEventBus);
        BABlocks.BLOCKS.register(modEventBus);
        //BABlocks.BLOCK_ITEMS.register(modEventBus);
        BABlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);

        BAScreenHandlers.MENUS.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(BananaArmoryTooltips::onItemTooltip);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            BananasArmoryClient.init(modEventBus);
        }
    }
}
