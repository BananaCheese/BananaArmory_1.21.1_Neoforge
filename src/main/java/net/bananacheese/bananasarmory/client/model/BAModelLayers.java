package net.bananacheese.bananasarmory.client.model;

import net.bananacheese.bananasarmory.BananasArmory;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class BAModelLayers {
    public static final ModelLayerLocation ARMOR = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(BananasArmory.MODID, "armor_frame"), "main");

    private BAModelLayers() {
    }
}
