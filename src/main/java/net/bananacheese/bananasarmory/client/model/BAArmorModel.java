package net.bananacheese.bananasarmory.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class BAArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
    public final ModelPart gorget;
    public final ModelPart fauld;
    public final ModelPart pauldronLeft;
    public final ModelPart pauldronRight;

    public BAArmorModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.gorget = body.getChild("gorget");
        this.fauld = body.getChild("fauld");
        this.pauldronRight = root.getChild("right_arm").getChild("pauldron_right");
        this.pauldronLeft = root.getChild("left_arm").getChild("pauldron_left");
    }

    public static LayerDefinition createBodyLayer() {
        // CubeDeformation(0.5F) matches vanilla's OUTER armor layer sizing
        // (slightly larger than the body/inner layer so it sits visibly
        // over the player model) — same value vanilla uses for its own
        // second armor layer (leggings use ~0.5F less than chest/etc, but
        // 0.5F is a reasonable single starting point for all four frames).
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.5F), 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        PartDefinition rightArm = root.getChild("right_arm");
        PartDefinition leftArm = root.getChild("left_arm");

        // --- PLACEHOLDER: Gorget (neck/upper-chest collar) ---
        body.addOrReplaceChild("gorget",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -2.0F, -2.5F, 8.0F, 3.0F, 5.0F),
                PartPose.ZERO);

        // --- PLACEHOLDER: Fauld (hip/waist skirt plates) ---
        body.addOrReplaceChild("fauld",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-4.5F, 9.0F, -2.5F, 9.0F, 4.0F, 5.0F),
                PartPose.ZERO);

        // --- PLACEHOLDER: Pauldrons (shoulder plates, one per arm) ---
        rightArm.addOrReplaceChild("pauldron_right",
                CubeListBuilder.create()
                        .texOffs(20, 0)
                        .addBox(-3.0F, -2.5F, -3.0F, 5.0F, 4.0F, 6.0F),
                PartPose.offset(-1.0F, 0.0F, 0.0F));

        leftArm.addOrReplaceChild("pauldron_left",
                CubeListBuilder.create()
                        .texOffs(20, 12)
                        .addBox(-2.0F, -2.5F, -3.0F, 5.0F, 4.0F, 6.0F),
                PartPose.offset(1.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}

