package net.bananacheese.bananasarmory.client.renderer;

import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@OnlyIn(Dist.CLIENT)
public class BAArmorRenderer extends GeoArmorRenderer<ArmorFrameItem> {

    public BAArmorRenderer(ArmorFrameItem.ArmorFrameType frameType) {
        // DefaultedItemGeoModel resolves this to:
        //   assets/barmory/geo/item/armor/<frametype>_frame.geo.json
        //   assets/barmory/textures/item/armor/<frametype>_frame.png
        //   assets/barmory/animations/item/armor/<frametype>_frame.animation.json (optional)
        super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(
                BananasArmory.MODID, "armor/" + frameType.name().toLowerCase() + "_frame")));
    }

    /**
     * Handed to ArmorFrameItem.createRenderer(...); lazily builds and
     * caches one BAArmorRenderer per item instance (each ArmorFrameItem
     * instance already has a fixed frameType, so this always resolves to
     * the correct geo model for that specific piece).
     *
     * BEST-GUESS interface/method — see the big warning above. If
     * GeoRenderProvider or getGeoArmorRenderer(...) don't exist/match on
     * your GeckoLib version, this is exactly where to paste the real
     * signature back to me.
     */
    public static class Provider implements GeoRenderProvider {
        private final ArmorFrameItem.ArmorFrameType frameType;
        private BAArmorRenderer renderer;

        public Provider(ArmorFrameItem.ArmorFrameType frameType) {
            this.frameType = frameType;
        }

        @Override
        public GeoArmorRenderer<?> getGeoArmorRenderer(ItemStack stack, EquipmentSlot slot,
                                                       boolean isSecondLayer, Object originalModel) {
            if (this.renderer == null) {
                this.renderer = new BAArmorRenderer(frameType);
            }
            return this.renderer;
        }
    }
}
