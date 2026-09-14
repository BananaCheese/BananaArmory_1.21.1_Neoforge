package net.bananacheese.bananasarmory.client.model;

import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ArmorComponentsProperty {

    public static int calculatePredicateValue(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorFrameItem)) {
            return 0;
        }

        List<ArmorFrameItem.ComponentData> components = ArmorFrameItem.getComponents(stack);

        if (components.isEmpty()) {
            return 0; // No components = use base model
        }

        return calculatePredicateValue(components);
    }

    private static int calculatePredicateValue(List<ArmorFrameItem.ComponentData> components) {
        List<String> componentNames = new ArrayList<>();
        for (ArmorFrameItem.ComponentData comp : components) {
            String id = comp.id();
            String itemName = id.substring(id.lastIndexOf(':') + 1);
            componentNames.add(itemName);
        }

        Collections.sort(componentNames);

        String combined = String.join("|", componentNames);

        int hash = combined.hashCode();
        int value = Math.abs(hash) % 9999;

        return Math.max(value, 1);
    }

    private ArmorComponentsProperty() {
    }
}
