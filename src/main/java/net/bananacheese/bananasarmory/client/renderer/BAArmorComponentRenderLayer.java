package net.bananacheese.bananasarmory.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.HashMap;
import java.util.Map;

public class BAArmorComponentRenderLayer extends GeoRenderLayer<ArmorFrameItem> {
    private final Map<String, GeoModel<ArmorFrameItem>> componentModels = new HashMap<>();

    public BAArmorComponentRenderLayer(BAArmorRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, ArmorFrameItem animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        BAArmorRenderer armorRenderer = (BAArmorRenderer) this.renderer;
        ItemStack stack = armorRenderer.getCurrentStack();

        if (stack == null) {
            return;
        }

        for (ArmorFrameItem.ComponentData comp : ArmorFrameItem.getComponents(stack)) {
            String name = comp.id().substring(comp.id().lastIndexOf(':') + 1);
            GeoModel<ArmorFrameItem> componentModel = getOrCreateComponentModel(name);

            ResourceLocation modelResource = componentModel.getModelResource(animatable, armorRenderer);
            BakedGeoModel bakedComponentModel = componentModel.getBakedModel(modelResource);
            ResourceLocation componentTexture = componentModel.getTextureResource(animatable, armorRenderer);
            RenderType componentRenderType = armorRenderer.getRenderType(animatable, componentTexture, bufferSource, partialTick);
            VertexConsumer componentBuffer = bufferSource.getBuffer(componentRenderType);

            armorRenderer.reRender(bakedComponentModel, poseStack, bufferSource, animatable, componentRenderType,
                    componentBuffer, partialTick, packedLight, packedOverlay, -1);
        }
    }

    private GeoModel<ArmorFrameItem> getOrCreateComponentModel(String componentName) {
        return componentModels.computeIfAbsent(componentName, name ->
                new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(
                        BananasArmory.MODID, "armor/components/geometry/" + name)));
    }
}

