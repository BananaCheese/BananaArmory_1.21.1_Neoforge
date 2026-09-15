package net.bananacheese.bananasarmory.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bananacheese.bananasarmory.BananasArmory;
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

import java.util.List;

public class BAArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final ModelPart helmet;
    private final ModelPart chestplate;
    private final ModelPart leggingsLeft;
    private final ModelPart leggingsRight;
    private final ModelPart bootsLeft;
    private final ModelPart bootsRight;
    private final ModelPart gorget;
    private final ModelPart fauld;
    private final ModelPart pauldronLeft;
    private final ModelPart pauldronRight;

    public BAArmorLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet) {
        super(parent);

        this.helmet = modelSet.bakeLayer(BAModelLayers.HELMET_FRAME).getChild("shape");
        this.chestplate = modelSet.bakeLayer(BAModelLayers.CHESTPLATE_FRAME).getChild("shape");

        ModelPart leggingsRoot = modelSet.bakeLayer(BAModelLayers.LEGGINGS_FRAME);
        this.leggingsLeft = leggingsRoot.getChild("leg_left");
        this.leggingsRight = leggingsRoot.getChild("leg_right");

        ModelPart bootsRoot = modelSet.bakeLayer(BAModelLayers.BOOTS_FRAME);
        this.bootsLeft = bootsRoot.getChild("boot_left");
        this.bootsRight = bootsRoot.getChild("boot_right");

        this.gorget = modelSet.bakeLayer(BAModelLayers.GORGET).getChild("shape");
        this.fauld = modelSet.bakeLayer(BAModelLayers.FAULD).getChild("shape");

        ModelPart pauldronsRoot = modelSet.bakeLayer(BAModelLayers.PAULDRONS);
        this.pauldronLeft = pauldronsRoot.getChild("pauldron_left");
        this.pauldronRight = pauldronsRoot.getChild("pauldron_right");
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

        M parentModel = this.getParentModel();

        // NOTE: no base-piece rendering redundancy to worry about here —
        // vanilla's own armor layer renders the base shape using whatever
        // texture BAArmorClientExtensions hands back. This layer only
        // handles pieces that need their own independent geometry.
        ResourceLocation baseTexture = DynamicTextureManager.getOrCreateWornArmorTexture(stack);
        if (baseTexture != null) {
            var baseConsumer = buffer.getBuffer(RenderType.armorCutoutNoCull(baseTexture));
            switch (slot) {
                case HEAD -> {
                    copyTransform(helmet, parentModel.head);
                    helmet.render(poseStack, baseConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                }
                case CHEST -> {
                    copyTransform(chestplate, parentModel.body);
                    chestplate.render(poseStack, baseConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                }
                case LEGS -> {
                    copyTransform(leggingsLeft, parentModel.leftLeg);
                    leggingsLeft.render(poseStack, baseConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                    copyTransform(leggingsRight, parentModel.rightLeg);
                    leggingsRight.render(poseStack, baseConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                }
                case FEET -> {
                    copyTransform(bootsLeft, parentModel.leftLeg);
                    bootsLeft.render(poseStack, baseConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                    copyTransform(bootsRight, parentModel.rightLeg);
                    bootsRight.render(poseStack, baseConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                }
                default -> {
                }
            }
        }

        for (ArmorFrameItem.ComponentData comp : ArmorFrameItem.getComponents(stack)) {
            String name = comp.id().substring(comp.id().lastIndexOf(':') + 1);
            List<PosedPart> parts = getGeometryParts(name, parentModel);
            if (parts.isEmpty()) {
                continue; // flat reskin only, already baked into the base texture
            }

            ResourceLocation compTexture = ResourceLocation.fromNamespaceAndPath(BananasArmory.MODID,
                    "textures/models/armor/components/geometry/" + name + ".png");
            var compConsumer = buffer.getBuffer(RenderType.armorCutoutNoCull(compTexture));

            for (PosedPart pp : parts) {
                copyTransform(pp.part(), pp.anchor());
                pp.part().render(poseStack, compConsumer, packedLight, OverlayTexture.NO_OVERLAY);
            }
        }
    }

    /**
     * Copies the anchor's CURRENT pose (already computed for this frame by
     * the wearer's own model) onto our independent piece. This is what
     * makes the piece follow head look / walking / arm swing without our
     * piece needing to be part of a HumanoidModel tree.
     */
    private static void copyTransform(ModelPart target, ModelPart anchor) {
        target.x = anchor.x;
        target.y = anchor.y;
        target.z = anchor.z;
        target.xRot = anchor.xRot;
        target.yRot = anchor.yRot;
        target.zRot = anchor.zRot;
    }

    /**
     * Maps a component's item name to the geometry part(s) it controls,
     * each paired with the vanilla body part it should follow. Empty list =
     * this component has no dedicated geometry (flat reskin only, handled
     * entirely by the base composited texture instead).
     *
     * Add an entry here every time you give a new component its own
     * geometry in BAArmorModels.
     */
    private List<PosedPart> getGeometryParts(String componentName, M parentModel) {
        return switch (componentName) {
            case "iron_gorget" -> List.of(new PosedPart(gorget, parentModel.body));
            case "iron_fauld" -> List.of(new PosedPart(fauld, parentModel.body));
            case "iron_pauldrons" -> List.of(
                    new PosedPart(pauldronLeft, parentModel.leftArm),
                    new PosedPart(pauldronRight, parentModel.rightArm)
            );
            default -> List.of(); // e.g. iron_reinforcement — flat reskin only
        };
    }

    private record PosedPart(ModelPart part, ModelPart anchor) {
    }
}
