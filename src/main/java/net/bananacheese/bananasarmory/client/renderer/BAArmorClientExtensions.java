package net.bananacheese.bananasarmory.client.renderer;

import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class BAArmorClientExtensions implements IClientItemExtensions {
    public static final BAArmorClientExtensions INSTANCE = new BAArmorClientExtensions();

    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorItem.Type type, ResourceLocation original) {
        if (!(stack.getItem() instanceof ArmorFrameItem)) {
            return original;
        }

        ResourceLocation composited = DynamicTextureManager.getOrCreateWornArmorTexture(stack);

        return composited != null ? composited : original;
    }

    private BAArmorClientExtensions() {
    }
}

