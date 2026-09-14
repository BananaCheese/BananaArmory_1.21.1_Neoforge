package net.bananacheese.bananasarmory.client.model;

import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * ADAPTED, NOT A DIRECT PORT — please read.
 * ============================================================================
 * The original implements `NumericProperty`, a client item-model property
 * type that plugs into the "range_dispatch" item model system. As best I can
 * tell, that whole system (and the `NumericProperty` interface/registry)
 * was introduced in a 1.21.x release after 1.21.1 as part of the item model
 * rework — it doesn't exist on 1.21.1.
 *
 * On 1.21.1, the equivalent way to pick a model variant based on item state
 * is the older, integer-based `CUSTOM_MODEL_DATA` component (a single int,
 * not the richer multi-field record from later versions) plus `overrides`
 * in the item's model JSON (assets/gearforge/models/item/helmet_frame.json)
 * matched with `stack value >= predicate value`, e.g.:
 *
 *   "overrides": [
 *     { "predicate": { "custom_model_data": 4213 }, "model": "gearforge:item/helmet_frame_variant_a" }
 *   ]
 *
 * (The original computed a 0.0–1.0 float for the newer range_dispatch
 * system; this port scales that same hash into a plain int instead, since
 * pre-1.21.4 CUSTOM_MODEL_DATA predicates are integer threshold matches, not
 * float ranges.)
 *
 * So this class is now a plain static helper (same hash calculation as the
 * original, kept identical so existing model override values you may already
 * have generated with ArmorModelJsonGenerator stay valid) instead of an
 * implementation of an interface that doesn't exist yet. It's called from
 * ArmorFrameAttributeModifiers.updateAttributes(...) — the same place that
 * already recomputes defense/toughness whenever components change — so the
 * custom model data always reflects the current component combination.
 *
 * Verify locally whether `NumericProperty`/`range_dispatch` actually exists
 * in 1.21.1 before assuming this fallback is necessary; if it does exist,
 * this can go back to closely matching the original.
 * ============================================================================
 */
public final class ArmorComponentsProperty {

    /**
     * Calculates a unique value based on attached components, for use as
     * CustomModelData's wrapped int.
     */
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
