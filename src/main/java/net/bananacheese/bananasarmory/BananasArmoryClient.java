package net.bananacheese.bananasarmory;

import net.bananacheese.bananasarmory.client.model.BAArmorModels;
import net.bananacheese.bananasarmory.client.model.BAModelLayers;
import net.bananacheese.bananasarmory.client.renderer.BAArmorClientExtensions;
import net.bananacheese.bananasarmory.client.renderer.BAArmorLayer;
import net.bananacheese.bananasarmory.item.BAItems;
import net.bananacheese.bananasarmory.screen.BAScreenHandlers;
import net.bananacheese.bananasarmory.screen.GearForgeScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@OnlyIn(Dist.CLIENT)
public class BananasArmoryClient {

    public static void init(IEventBus modBus) {
        modBus.addListener(BananasArmoryClient::registerScreens);
        modBus.addListener(BananasArmoryClient::registerClientExtensions);
        modBus.addListener(BananasArmoryClient::registerLayerDefinitions);
        modBus.addListener(BananasArmoryClient::addLayers);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BAScreenHandlers.GEAR_FORGE_MENU.get(), GearForgeScreen::new);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(BAArmorClientExtensions.INSTANCE,
                BAItems.HELMET_FRAME.get(),
                BAItems.CHESTPLATE_FRAME.get(),
                BAItems.LEGGINGS_FRAME.get(),
                BAItems.BOOTS_FRAME.get());
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BAModelLayers.HELMET_FRAME, BAArmorModels::createHelmetLayer);
        event.registerLayerDefinition(BAModelLayers.CHESTPLATE_FRAME, BAArmorModels::createChestplateLayer);
        event.registerLayerDefinition(BAModelLayers.LEGGINGS_FRAME, BAArmorModels::createLeggingsLayer);
        event.registerLayerDefinition(BAModelLayers.BOOTS_FRAME, BAArmorModels::createBootsLayer);
        event.registerLayerDefinition(BAModelLayers.GORGET, BAArmorModels::createGorgetLayer);
        event.registerLayerDefinition(BAModelLayers.FAULD, BAArmorModels::createFauldLayer);
        event.registerLayerDefinition(BAModelLayers.PAULDRONS, BAArmorModels::createPauldronsLayer);
    }

    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer<? extends Player> renderer = event.getSkin(skin);
            if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                addArmorLayerUnchecked(livingRenderer, event.getEntityModels());
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addArmorLayerUnchecked(LivingEntityRenderer renderer, net.minecraft.client.model.geom.EntityModelSet modelSet) {
        renderer.addLayer(new BAArmorLayer(renderer, modelSet));
    }

    private BananasArmoryClient() {
    }
}
