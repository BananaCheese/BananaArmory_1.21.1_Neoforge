package net.bananacheese.bananasarmory.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public final class BAArmorModels {

    public static LayerDefinition createHelmetLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createChestplateLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5F, 0.0F, -2.5F, 9.0F, 9.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }


     //Two children sharing one texture — "leg_left"/"leg_right" — each
     //independently posed to follow its own vanilla leg (so walking
     //animates each side correctly, same as vanilla's own leggings layer).

    public static LayerDefinition createLeggingsLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("leg_left",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("leg_right",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createBootsLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("boot_left",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, 9.0F, -2.5F, 5.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("boot_right",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-2.5F, 9.0F, -2.5F, 5.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 24, 24);
    }

    public static LayerDefinition createGorgetLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -2.0F, -3.0F, 8.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 16, 16);
    }

    public static LayerDefinition createFauldLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5F, 9.0F, -3.0F, 9.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 16, 16);
    }


    //Two children sharing one texture, mirrored — "pauldron_left"/"pauldron_right".

    public static LayerDefinition createPauldronsLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("pauldron_left",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, -2.5F, -3.0F, 5.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("pauldron_right",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-2.5F, -2.5F, -3.0F, 5.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 16, 16);
    }

    private BAArmorModels() {
    }
}
