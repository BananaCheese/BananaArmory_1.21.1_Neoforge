package net.bananacheese.bananasarmory.item.custom;

import net.bananacheese.bananasarmory.client.renderer.BAArmorRenderer;
import net.bananacheese.bananasarmory.screen.GearForgeLayout;
import net.bananacheese.bananasarmory.screen.GearForgeable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ArmorFrameItem extends ArmorItem implements GeoItem, GearForgeable {
    private final ArmorFrameType frameType;
    private static final int BASE_DEFENSE = 2;
    private static final int BASE_DURABILITY = 100;
    private static final double BASE_TOUGHNESS = 0.0;

    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);

    public ArmorFrameItem(Properties properties, ArmorFrameType frameType, Holder<ArmorMaterial> baseMaterial) {
        // ArmorItem still gives us the equip-slot behavior (right click to
        // wear, slot restriction). Its own default attribute modifiers get
        // fully overwritten by updateAttributes(...) below the moment a
        // component is added/removed, so `baseMaterial` mostly just needs
        // to exist and match the frame's equipment Type.
        super(baseMaterial, mapType(frameType), properties.durability(BASE_DURABILITY));
        this.frameType = frameType;
        GeoItem.registerSyncedAnimatable(this);
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

    // --- GeoRenderProvider ---

    /**
     * The confirmed-working registration pattern (recovered from git
     * history — this got accidentally reverted to a broken direct-
     * `implements GeoRenderProvider` version during an earlier edit, which
     * is what caused worn rendering to fall back to plain vanilla iron).
     * GeckoLib expects THIS entrypoint — createGeoRenderer handing a
     * GeoRenderProvider instance to the consumer — not the item
     * implementing GeoRenderProvider itself.
     */
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack,
                                                                                 @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null) // Important that we do this. If we just instantiate it directly in the field it can cause incompatibilities with some mods.
                    this.renderer = new BAArmorRenderer(frameType);

                return this.renderer;
            }
        });
    }

    // --- GearForgeable ---

    /**
     * Same 6 slots, same positions, same validity rule as before — just
     * expressed as a GearForgeLayout now instead of being hardcoded
     * directly in GearForgeMenu/GearForgeSlot. Positions are identical
     * across all four ArmorFrameTypes; only which components are valid
     * differs, via ComponentType.isCompatibleWith(this.frameType).
     */
    @Override
    public GearForgeLayout getForgeLayout() {
        java.util.function.Predicate<ItemStack> componentValid = stack ->
                stack.getItem() instanceof ArmorComponentItem component
                        && component.getComponentType().isCompatibleWith(this.frameType);

        return new GearForgeLayout(
                ResourceLocation.fromNamespaceAndPath("barmory", "textures/gui/gear_forge.png"),
                List.of(
                        new GearForgeLayout.SlotDef("component_1", 101, 17, componentValid),
                        new GearForgeLayout.SlotDef("component_2", 125, 17, componentValid),
                        new GearForgeLayout.SlotDef("component_3", 89, 38, componentValid),
                        new GearForgeLayout.SlotDef("component_4", 137, 38, componentValid),
                        new GearForgeLayout.SlotDef("component_5", 101, 59, componentValid),
                        new GearForgeLayout.SlotDef("component_6", 125, 59, componentValid)
                )
        );
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