package net.bananacheese.bananasarmory.item.custom;

import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.client.model.ArmorComponentsProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.List;

public class ArmorFrameAttributeModifiers {

    public static void updateAttributes(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorFrameItem frameItem)) {
            return;
        }

        int totalDefense = ArmorFrameItem.getTotalDefense(stack);
        int totalDurability = ArmorFrameItem.getTotalDurability(stack);
        double totalToughness = ArmorFrameItem.getTotalToughness(stack);

        stack.set(DataComponents.MAX_DAMAGE, totalDurability);

        List<ItemAttributeModifiers.Entry> modifiers = new ArrayList<>();

        EquipmentSlot slot = frameItem.getFrameType().getSlot();
        EquipmentSlotGroup modifierSlot = EquipmentSlotGroup.bySlot(slot);

        ResourceLocation armorId = ResourceLocation.fromNamespaceAndPath(
                BananasArmory.MODID, "armor_frame_defense_" + slot.getName());
        ResourceLocation toughnessId = ResourceLocation.fromNamespaceAndPath(
                BananasArmory.MODID, "armor_frame_toughness_" + slot.getName());

        modifiers.add(new ItemAttributeModifiers.Entry(
                Attributes.ARMOR,
                new AttributeModifier(armorId, totalDefense, AttributeModifier.Operation.ADD_VALUE),
                modifierSlot
        ));

        modifiers.add(new ItemAttributeModifiers.Entry(
                Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(toughnessId, totalToughness, AttributeModifier.Operation.ADD_VALUE),
                modifierSlot
        ));

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(modifiers, true));

        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(ArmorComponentsProperty.calculatePredicateValue(stack)));
    }
}