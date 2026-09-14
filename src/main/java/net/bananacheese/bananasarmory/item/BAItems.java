package net.bananacheese.bananasarmory.item;

import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.block.custom.GearForgeBlock;
import net.bananacheese.bananasarmory.item.custom.ArmorComponentItem;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.bananacheese.bananasarmory.item.custom.GearForgeHammer;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry skeleton for the gearforge-owned items from the original DAItems:
 * GEAR_FORGE_HAMMER, the four ArmorFrameItems, and the four ArmorComponentItems.
 *
 * These are still plain Item placeholders. Next step: port GearForgeHammer,
 * ArmorFrameItem (+ ArmorFrameType enum) and ArmorComponentItem (+ ComponentType
 * enum + group logic) into net.bananacheese.gearforge.item, then swap the
 * `new Item(props)` calls below for `new GearForgeHammer(props)` etc.
 */
public final class BAItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(BananasArmory.MODID);

    public static final DeferredItem<GearForgeHammer> GEAR_FORGE_HAMMER = ITEMS.registerItem(
            "gear_forge_hammer", props -> new GearForgeHammer(props.stacksTo(1).durability(250))
    );

    // --- Armor frames (wearable, customized in the Gear Forge) ---
    public static final DeferredItem<ArmorFrameItem> HELMET_FRAME = ITEMS.registerItem(
            "helmet_frame", props -> new ArmorFrameItem(props.stacksTo(1), ArmorFrameItem.ArmorFrameType.HELMET, ArmorMaterials.IRON));
    public static final DeferredItem<ArmorFrameItem> CHESTPLATE_FRAME = ITEMS.registerItem(
            "chestplate_frame", props -> new ArmorFrameItem(props.stacksTo(1), ArmorFrameItem.ArmorFrameType.CHESTPLATE, ArmorMaterials.IRON));
    public static final DeferredItem<ArmorFrameItem> LEGGINGS_FRAME = ITEMS.registerItem(
            "leggings_frame", props -> new ArmorFrameItem(props.stacksTo(1), ArmorFrameItem.ArmorFrameType.LEGGINGS, ArmorMaterials.IRON));
    public static final DeferredItem<ArmorFrameItem> BOOTS_FRAME = ITEMS.registerItem(
            "boots_frame", props -> new ArmorFrameItem(props.stacksTo(1), ArmorFrameItem.ArmorFrameType.BOOTS, ArmorMaterials.IRON));

    // --- Armor components (attach into frame slots) ---
    public static final DeferredItem<ArmorComponentItem> IRON_PAULDRONS = ITEMS.registerItem(
            "iron_pauldrons", props -> new ArmorComponentItem(props.stacksTo(16),
                    ArmorComponentItem.ComponentType.PAULDRONS, "pauldrons", 2, 50, 0.5));
    public static final DeferredItem<ArmorComponentItem> IRON_GORGET = ITEMS.registerItem(
            "iron_gorget", props -> new ArmorComponentItem(props.stacksTo(16),
                    ArmorComponentItem.ComponentType.GORGET, "gorget", 1, 30, 0.3));
    public static final DeferredItem<ArmorComponentItem> IRON_FAULD = ITEMS.registerItem(
            "iron_fauld", props -> new ArmorComponentItem(props.stacksTo(16),
                    ArmorComponentItem.ComponentType.FAULD, "fauld", 2, 40, 0.4));
    public static final DeferredItem<ArmorComponentItem> IRON_REINFORCEMENT = ITEMS.registerItem(
            "iron_reinforcement", props -> new ArmorComponentItem(props.stacksTo(16),
                    ArmorComponentItem.ComponentType.REINFORCEMENT, "reinforcement", 1, 100, 1.0));

    private BAItems() {
    }
}

