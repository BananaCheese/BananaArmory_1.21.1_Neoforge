package net.bananacheese.bananasarmory.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.HashMap;
import java.util.List;
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
        HumanoidModel<?> wearerModel = armorRenderer.getWearerBaseModel();

        if (stack == null || wearerModel == null) {
            return;
        }

        for (ArmorFrameItem.ComponentData comp : ArmorFrameItem.getComponents(stack)) {
            String name = comp.id().substring(comp.id().lastIndexOf(':') + 1);
            List<BoneAnchor> anchors = getBoneAnchors(name, wearerModel);
            if (anchors.isEmpty()) {
                continue; // flat reskin only, no dedicated geometry
            }

            GeoModel<ArmorFrameItem> componentModel = getOrCreateComponentModel(name);
            ResourceLocation modelResource = componentModel.getModelResource(animatable, armorRenderer);
            BakedGeoModel bakedComponentModel = componentModel.getBakedModel(modelResource);

            for (BoneAnchor anchor : anchors) {
                GeoBone bone = findBone(bakedComponentModel.topLevelBones(), anchor.boneName());
                if (bone == null) {
                    BananasArmory.LOGGER.warn("Component geo model '" + name + "' has no bone named '"
                            + anchor.boneName() + "' — pose won't follow the wearer for this piece.");
                    continue;
                }

                // Same transform-copy GeoArmorRenderer.applyBaseTransformations
                // does internally, just targeted at the correct bone object.
                // The offset formula differs per body part — GeckoLib's own
                // code uses plain (x, -y, z) for head/body, but arms need an
                // extra +/-5 on X and "2 - y" instead of plain negation (legs
                // similarly need +/-2 and "12 - y"). Get this wrong and the
                // piece renders offset/floating even though rotation is fine.
                ModelPart part = anchor.modelPart();
                RenderUtil.matchModelPartRot(part, bone);
                switch (anchor.type()) {
                    case HEAD, BODY -> bone.updatePosition(part.x, -part.y, part.z);
                    case RIGHT_ARM -> bone.updatePosition(part.x + 5, 2 - part.y, part.z);
                    case LEFT_ARM -> bone.updatePosition(part.x - 5, 2 - part.y, part.z);
                    case RIGHT_LEG -> bone.updatePosition(part.x + 2, 12 - part.y, part.z);
                    case LEFT_LEG -> bone.updatePosition(part.x - 2, 12 - part.y, part.z);
                }
            }

            ResourceLocation componentTexture = componentModel.getTextureResource(animatable, armorRenderer);
            RenderType componentRenderType = armorRenderer.getRenderType(animatable, componentTexture, bufferSource, partialTick);
            VertexConsumer componentBuffer = bufferSource.getBuffer(componentRenderType);

            armorRenderer.reRender(bakedComponentModel, poseStack, bufferSource, animatable, componentRenderType,
                    componentBuffer, partialTick, packedLight, packedOverlay, -1);
        }
    }

    /**
     * Maps a component's item name to the bone(s) it needs posed, each
     * paired with the vanilla ModelPart it should follow. Bone names here
     * are whatever you actually named them in that component's Blockbench
     * project — these are just the strings this code searches for, adjust
     * to match exactly.
     */
    private List<BoneAnchor> getBoneAnchors(String componentName, HumanoidModel<?> wearerModel) {
        return switch (componentName) {
            case "iron_gorget" -> List.of(new BoneAnchor("gorget", wearerModel.body, AnchorType.BODY));
            case "iron_fauld" -> List.of(new BoneAnchor("fauld", wearerModel.body, AnchorType.BODY));
            case "iron_pauldrons" -> List.of(
                    new BoneAnchor("pauldronLeft", wearerModel.leftArm, AnchorType.LEFT_ARM),
                    new BoneAnchor("pauldronRight", wearerModel.rightArm, AnchorType.RIGHT_ARM)
            );
            default -> List.of(); // e.g. iron_reinforcement — flat reskin only
        };
    }

    private GeoBone findBone(List<GeoBone> bones, String name) {
        for (GeoBone bone : bones) {
            if (bone.getName().equals(name)) {
                return bone;
            }
            GeoBone found = findBone(bone.getChildBones(), name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private GeoModel<ArmorFrameItem> getOrCreateComponentModel(String componentName) {
        return componentModels.computeIfAbsent(componentName, name ->
                new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(
                        BananasArmory.MODID, "armor/components/geometry/" + name)));
    }

    private record BoneAnchor(String boneName, ModelPart modelPart, AnchorType type) {
    }

    /**
     * Which vanilla body part this bone follows — determines which
     * position-offset formula applies (see the switch in render() above).
     * Values/offsets copied directly from GeoArmorRenderer's own
     * applyBaseTransformations, which uses a different fixed correction per
     * body part (head/body use plain negation; arms/legs need extra
     * +/-5 or +/-2 offsets to align with GeckoLib's armor bone coordinate
     * space).
     */
    private enum AnchorType {
        HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG
    }
}
