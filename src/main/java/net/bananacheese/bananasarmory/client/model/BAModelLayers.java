package net.bananacheese.bananasarmory.client.model;

import net.bananacheese.bananasarmory.BananasArmory;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public final class BAModelLayers {
    public static final ModelLayerLocation HELMET_FRAME = layer("helmet_frame");
    public static final ModelLayerLocation CHESTPLATE_FRAME = layer("chestplate_frame");
    public static final ModelLayerLocation LEGGINGS_FRAME = layer("leggings_frame");
    public static final ModelLayerLocation BOOTS_FRAME = layer("boots_frame");
    public static final ModelLayerLocation GORGET = layer("gorget");
    public static final ModelLayerLocation FAULD = layer("fauld");
    public static final ModelLayerLocation PAULDRONS = layer("pauldrons");

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(BananasArmory.MODID, name), "main");
    }

    private BAModelLayers() {
    }
}

