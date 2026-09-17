package net.bananacheese.bananasarmory.client.renderer;

import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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

        // Renders each attached component as its own independent sub-model
        // pass (own geo file, own texture) layered on top of this base
        // frame render — see BAArmorComponentRenderLayer for why this
        // doesn't need combinatorial baking.
        this.addRenderLayer(new BAArmorComponentRenderLayer(this));
    }
}
