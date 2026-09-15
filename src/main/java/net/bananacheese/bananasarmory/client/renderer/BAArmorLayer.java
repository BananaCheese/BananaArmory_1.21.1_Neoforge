package net.bananacheese.bananasarmory.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.client.model.BAArmorModel;
import net.bananacheese.bananasarmory.client.model.BAModelLayers;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class BAArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final BAArmorModel<T> model;

    public BAArmorLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new BAArmorModel<>(modelSet.bakeLayer(BAModelLayers.ARMOR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        renderSlot(poseStack, buffer, packedLight, entity, EquipmentSlot.HEAD);
        renderSlot(poseStack, buffer, packedLight, entity, EquipmentSlot.CHEST);
        renderSlot(poseStack, buffer, packedLight, entity, EquipmentSlot.LEGS);
        renderSlot(poseStack, buffer, packedLight, entity, EquipmentSlot.FEET);
    }

    private void renderSlot(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, EquipmentSlot slot) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (!(stack.getItem() instanceof ArmorFrameItem frameItem) || frameItem.getFrameType().getSlot() != slot) {
            return;
        }

        // Same pose-copying trick vanilla's HumanoidArmorLayer uses, so our
        // separate model instance moves exactly like the entity's own body.
        this.getParentModel().copyPropertiesTo(model);
        model.setAllVisible(false);

        int layer = (slot == EquipmentSlot.LEGS) ? 2 : 1;
        ResourceLocation baseTexture = DynamicTextureManager.getOrCreateWornArmorTexture(stack, layer);

        if (baseTexture != null) {
            setBasePartsVisible(slot, true);
            model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorCutoutNoCull(baseTexture)),
                    packedLight, OverlayTexture.NO_OVERLAY);
            setBasePartsVisible(slot, false);
        }

        for (ArmorFrameItem.ComponentData comp : ArmorFrameItem.getComponents(stack)) {
            String name = comp.id().substring(comp.id().lastIndexOf(':') + 1);
            ModelPart[] parts = getGeometryParts(name);
            if (parts.length == 0) {
                continue; // flat reskin only, already baked into baseTexture above
            }

            ResourceLocation compTexture = ResourceLocation.fromNamespaceAndPath(BananasArmory.MODID,
                    "textures/models/armor/components/geometry/" + name + ".png");

            for (ModelPart part : parts) {
                part.visible = true;
            }
            model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorCutoutNoCull(compTexture)),
                    packedLight, OverlayTexture.NO_OVERLAY);
            for (ModelPart part : parts) {
                part.visible = false;
            }
        }
    }

    private void setBasePartsVisible(EquipmentSlot slot, boolean visible) {
        switch (slot) {
            case HEAD -> {
                model.head.visible = visible;
                model.hat.visible = visible;
            }
            case CHEST -> {
                model.body.visible = visible;
                model.rightArm.visible = visible;
                model.leftArm.visible = visible;
            }
            case LEGS -> {
                model.body.visible = visible;
                model.rightLeg.visible = visible;
                model.leftLeg.visible = visible;
            }
            case FEET -> {
                model.rightLeg.visible = visible;
                model.leftLeg.visible = visible;
            }
            default -> {
            }
        }
    }

    /**
     * Maps a component's item name to the geometry part(s) it controls.
     * Empty array = this component has no dedicated geometry (flat reskin
     * only, handled entirely by the base composited texture instead).
     *
     * Add an entry here every time you give a new component its own
     * geometry in BAArmorModel.
     */
    private ModelPart[] getGeometryParts(String componentName) {
        return switch (componentName) {
            case "iron_gorget" -> new ModelPart[]{model.gorget};
            case "iron_fauld" -> new ModelPart[]{model.fauld};
            case "iron_pauldrons" -> new ModelPart[]{model.pauldronLeft, model.pauldronRight};
            default -> new ModelPart[0]; // e.g. iron_reinforcement — flat reskin only
        };
    }
}