package net.bananacheese.bananasarmory;

import net.bananacheese.bananasarmory.screen.BAScreenHandlers;
import net.bananacheese.bananasarmory.screen.GearForgeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public class BananasArmoryClient {

    public static void init(IEventBus modBus) {
        modBus.addListener(BananasArmoryClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BAScreenHandlers.GEAR_FORGE_MENU.get(), GearForgeScreen::new);
    }

    private BananasArmoryClient() {
    }
}
