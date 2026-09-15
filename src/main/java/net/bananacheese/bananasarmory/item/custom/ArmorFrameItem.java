package net.bananacheese.bananasarmory.item.custom;

import net.bananacheese.bananasarmory.client.renderer.BAArmorRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ArmorFrameItem extends ArmorItem implements GeoItem {
    private final ArmorFrameType frameType;
    private static final int BASE_DEFENSE = 2;
    private static final int BASE_DURABILITY = 100;
    private static final double BASE_TOUGHNESS = 0.0;

    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public ArmorFrameItem(Properties properties, ArmorFrameType frameType, Holder<ArmorMaterial> baseMaterial) {
        // ArmorItem still gives us the equip-slot behavior (right click to
        // wear, slot restriction). Its own default attribute modifiers get
        // fully overwritten by updateAttributes(...) below the moment a
        // component is added/removed, so `baseMaterial` mostly just needs
        // to exist and match the frame's equipment Type.
        super(baseMaterial, mapType(frameType), properties.durability(BASE_DURABILITY));
        this.frameType = frameType;
    }

    private static Type mapType(ArmorFrameType frameType) {
        return switch (frameType) {
            case HELMET -> Type.HELMET;
            case CHESTPLATE -> Type.CHESTPLATE;
            case LEGGINGS -> Type.LEGGINGS;
            case BOOTS -> Type.BOOTS;
        };
    }

    // --- GeoItem / GeoAnimatable ---

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableCache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animations needed yet — geometry-only (static pose, following
        // the wearer's own body pose). Add a controller here later if you
        // want e.g. an idle shimmer or activation animation.
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new BAArmorRenderer.Provider(this.frameType));
    }

    // --- Frame-specific logic (unchanged from before) ---

    public ArmorFrameType getFrameType() {
        return frameType;
    }

    public static boolean addComponent(ItemStack frameStack, String componentId, String componentGroup,
                                       int defenseBonus, int durabilityBonus, double toughnessBonus) {
        CompoundTag nbt = frameStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (!nbt.contains("Components")) {
            nbt.put("Components", new ListTag());
        }

        ListTag components = nbt.getList("Components", 10); // 10 = CompoundTag id

        if (components.size() >= 6) {
            return false;
        }

        Set<String> existingGroups = new HashSet<>();
        for (int i = 0; i < components.size(); i++) {
            CompoundTag comp = components.getCompound(i);
            String group = comp.getString("Group");
            if (!group.isEmpty()) {
                existingGroups.add(group);
            }
        }

        if (existingGroups.contains(componentGroup)) {
            return false;
        }

        CompoundTag component = new CompoundTag();
        component.putString("Id", componentId);
        component.putString("Group", componentGroup);
        component.putInt("Defense", defenseBonus);
        component.putInt("Durability", durabilityBonus);
        component.putDouble("Toughness", toughnessBonus);

        components.add(component);
        nbt.put("Components", components);

        frameStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        return true;
    }

    public static List<ComponentData> getComponents(ItemStack frameStack) {
        List<ComponentData> result = new ArrayList<>();
        CompoundTag nbt = frameStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (nbt.contains("Components")) {
            ListTag components = nbt.getList("Components", 10);
            for (int i = 0; i < components.size(); i++) {
                CompoundTag component = components.getCompound(i);
                result.add(new ComponentData(
                        component.getString("Id"),
                        component.getString("Group"),
                        component.getInt("Defense"),
                        component.getInt("Durability"),
                        component.getDouble("Toughness")
                ));
            }
        }

        return result;
    }

    public static int getComponentCount(ItemStack frameStack) {
        CompoundTag nbt = frameStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt.contains("Components")) {
            return nbt.getList("Components", 10).size();
        }
        return 0;
    }

    public static int getTotalDefense(ItemStack frameStack) {
        int total = BASE_DEFENSE;
        for (ComponentData component : getComponents(frameStack)) {
            total += component.defenseBonus();
        }
        return total;
    }

    public static int getTotalDurability(ItemStack frameStack) {
        int total = BASE_DURABILITY;
        for (ComponentData component : getComponents(frameStack)) {
            total += component.durabilityBonus();
        }
        return total;
    }

    public static double getTotalToughness(ItemStack frameStack) {
        double total = BASE_TOUGHNESS;
        for (ComponentData component : getComponents(frameStack)) {
            total += component.toughnessBonus();
        }
        return total;
    }

    public enum ArmorFrameType {
        HELMET("Helmet", 6, EquipmentSlot.HEAD),
        CHESTPLATE("Chestplate", 6, EquipmentSlot.CHEST),
        LEGGINGS("Leggings", 6, EquipmentSlot.LEGS),
        BOOTS("Boots", 6, EquipmentSlot.FEET);

        private final String displayName;
        private final int upgradeSlots;
        private final EquipmentSlot slot;

        ArmorFrameType(String displayName, int upgradeSlots, EquipmentSlot slot) {
            this.displayName = displayName;
            this.upgradeSlots = upgradeSlots;
            this.slot = slot;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getUpgradeSlots() {
            return upgradeSlots;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }
    }

    public record ComponentData(String id, String group, int defenseBonus, int durabilityBonus, double toughnessBonus) {}
}
