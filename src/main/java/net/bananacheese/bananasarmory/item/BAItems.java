package net.bananacheese.bananasarmory.item;

import net.bananacheese.bananasarmory.BananasArmory;
import net.minecraft.world.item.Item;
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

    public static final DeferredItem<Item> GEAR_FORGE_HAMMER = ITEMS.registerItem(
            "gear_forge_hammer",
            props -> new Item(props.stacksTo(1).durability(250))
    );

    // --- Armor frames (wearable, customized in the Gear Forge) ---
    public static final DeferredItem<Item> HELMET_FRAME = ITEMS.registerItem(
            "helmet_frame", props -> new Item(props.stacksTo(1)));
    public static final DeferredItem<Item> CHESTPLATE_FRAME = ITEMS.registerItem(
            "chestplate_frame", props -> new Item(props.stacksTo(1)));
    public static final DeferredItem<Item> LEGGINGS_FRAME = ITEMS.registerItem(
            "leggings_frame", props -> new Item(props.stacksTo(1)));
    public static final DeferredItem<Item> BOOTS_FRAME = ITEMS.registerItem(
            "boots_frame", props -> new Item(props.stacksTo(1)));

    // --- Armor components (attach into frame slots) ---
    public static final DeferredItem<Item> IRON_PAULDRONS = ITEMS.registerItem(
            "iron_pauldrons", props -> new Item(props.stacksTo(16)));
    public static final DeferredItem<Item> IRON_GORGET = ITEMS.registerItem(
            "iron_gorget", props -> new Item(props.stacksTo(16)));
    public static final DeferredItem<Item> IRON_FAULD = ITEMS.registerItem(
            "iron_fauld", props -> new Item(props.stacksTo(16)));
    public static final DeferredItem<Item> IRON_REINFORCEMENT = ITEMS.registerItem(
            "iron_reinforcement", props -> new Item(props.stacksTo(16)));

    private BAItems() {
    }
}

