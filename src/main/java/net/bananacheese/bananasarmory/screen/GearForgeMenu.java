package net.bananacheese.bananasarmory.screen;

import net.bananacheese.bananasarmory.item.custom.ArmorComponentItem;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameAttributeModifiers;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.bananacheese.bananasarmory.screen.slot.GearForgeSlot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

public class GearForgeMenu extends AbstractContainerMenu {
    private final Container container;

    private static final int FRAME_SLOT = 0;
    private static final int UPGRADE_SLOTS_START = 1;
    public static final int MAX_UPGRADE_SLOTS = 8;
    private static final int INVENTORY_START = UPGRADE_SLOTS_START + MAX_UPGRADE_SLOTS;
    private static final int TOTAL_CONTAINER_SLOTS = INVENTORY_START;

    // Off-screen position for inactive upgrade slots — outside the GUI's
    // rendered bounds, so they're both invisible and unreachable by click.
    private static final int HIDDEN_SLOT_POS = -1000;

    private final List<GearForgeSlot> upgradeSlots = new ArrayList<>();
    private GearForgeLayout currentLayout;

    public GearForgeMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(TOTAL_CONTAINER_SLOTS));
    }

    public GearForgeMenu(int syncId, Inventory playerInventory, Container container) {
        super(BAScreenHandlers.GEAR_FORGE_MENU.get(), syncId);

        checkContainerSize(container, TOTAL_CONTAINER_SLOTS);
        this.container = container;
        this.currentLayout = GearForgeLayout.empty(defaultBackground());

        this.addSlot(new GearForgeSlot(container, FRAME_SLOT, 113, 38, GearForgeSlot.SlotRole.FRAME, -1, this));

        for (int i = 0; i < MAX_UPGRADE_SLOTS; i++) {
            GearForgeSlot slot = new GearForgeSlot(container, UPGRADE_SLOTS_START + i,
                    HIDDEN_SLOT_POS, HIDDEN_SLOT_POS, GearForgeSlot.SlotRole.UPGRADE, i, this);
            upgradeSlots.add(slot);
            this.addSlot(slot);
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 87 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 145));
        }

        updateLayout();
    }

    private static ResourceLocation defaultBackground() {
        return ResourceLocation.fromNamespaceAndPath("barmory", "textures/gui/gear_forge.png");
    }

    // --- Layout switching ---

    public GearForgeLayout getCurrentLayout() {
        return this.currentLayout;
    }

    public boolean isUpgradeSlotActive(int index) {
        return index < this.currentLayout.slots().size();
    }

    public boolean isValidForUpgradeSlot(int index, ItemStack stack) {
        if (!isUpgradeSlotActive(index)) {
            return false;
        }
        return this.currentLayout.slots().get(index).validItem().test(stack);
    }

    private void updateLayout() {
        ItemStack frameStack = container.getItem(FRAME_SLOT);
        GearForgeLayout newLayout = frameStack.getItem() instanceof GearForgeable forgeable
                ? forgeable.getForgeLayout()
                : GearForgeLayout.empty(defaultBackground());

        this.currentLayout = newLayout;

        // Slot.x/y are final in this version — can't mutate an existing
        // Slot's position, so each active/inactive change replaces the
        // Slot object at that same LIST POSITION instead. Vanilla's
        // click/render handling keys off position within this.slots (a
        // mutable NonNullList, even though the `slots` reference itself is
        // final), not any state stored on the Slot object, so this should
        // stay correctly in sync — but this is the one part of this whole
        // system I haven't been able to verify against real behavior, so
        // if slot interaction misbehaves specifically right after a layout
        // switch, this is the first place to look.
        for (int i = 0; i < MAX_UPGRADE_SLOTS; i++) {
            int containerIndex = UPGRADE_SLOTS_START + i;
            int x = HIDDEN_SLOT_POS;
            int y = HIDDEN_SLOT_POS;

            if (i < newLayout.slots().size()) {
                GearForgeLayout.SlotDef def = newLayout.slots().get(i);
                x = def.x();
                y = def.y();
            }

            GearForgeSlot newSlot = new GearForgeSlot(container, containerIndex, x, y,
                    GearForgeSlot.SlotRole.UPGRADE, i, this);
            this.slots.set(containerIndex, newSlot);
            upgradeSlots.set(i, newSlot);
        }
    }

    // --- Component application (still armor-specific, see class doc) ---

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        ItemStack frameBeforeClick = container.getItem(FRAME_SLOT).copy();
        boolean hadFrameBefore = frameBeforeClick.getItem() instanceof ArmorFrameItem;

        super.clicked(slotIndex, button, clickType, player);

        ItemStack frameAfterClick = container.getItem(FRAME_SLOT);
        boolean hasFrameAfter = frameAfterClick.getItem() instanceof ArmorFrameItem;

        if (slotIndex == FRAME_SLOT) {
            updateLayout();
        }

        if (!hadFrameBefore && hasFrameAfter) {
            syncFrameWithComponents();
        } else if (hadFrameBefore && !hasFrameAfter && slotIndex == FRAME_SLOT) {
            saveComponentsToFrameAndConsume(frameBeforeClick, player);

            for (int i = 0; i < MAX_UPGRADE_SLOTS; i++) {
                container.setItem(UPGRADE_SLOTS_START + i, ItemStack.EMPTY);
            }

            ItemStack cursorStack = getCarried();
            if (cursorStack.getItem() instanceof ArmorFrameItem) {
                setCarried(frameBeforeClick);
            }
        } else if (hasFrameAfter && slotIndex >= UPGRADE_SLOTS_START && slotIndex < INVENTORY_START) {
            updateFrameStatsPreview(player);
        }
    }

    private void syncFrameWithComponents() {
        ItemStack frameStack = container.getItem(FRAME_SLOT);

        if (frameStack.getItem() instanceof ArmorFrameItem) {
            List<ArmorFrameItem.ComponentData> components = ArmorFrameItem.getComponents(frameStack);

            for (int i = 0; i < currentLayout.slots().size(); i++) {
                if (i < components.size()) {
                    ArmorFrameItem.ComponentData data = components.get(i);
                    ResourceLocation itemId = ResourceLocation.tryParse(data.id());
                    if (itemId != null) {
                        Item item = BuiltInRegistries.ITEM.get(itemId);
                        if (item instanceof ArmorComponentItem) {
                            container.setItem(UPGRADE_SLOTS_START + i, new ItemStack(item, 1));
                        }
                    }
                } else {
                    container.setItem(UPGRADE_SLOTS_START + i, ItemStack.EMPTY);
                }
            }
        }
    }

    private void updateFrameStatsPreview(Player player) {
        ItemStack frameStack = container.getItem(FRAME_SLOT);
        if (frameStack.isEmpty() || !(frameStack.getItem() instanceof ArmorFrameItem)) {
            return;
        }

        CompoundTag nbt = new CompoundTag();
        nbt.put("Components", new ListTag());
        frameStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        boolean hadDuplicate = false;
        for (int i = 0; i < currentLayout.slots().size(); i++) {
            ItemStack componentStack = container.getItem(UPGRADE_SLOTS_START + i);
            if (componentStack.getItem() instanceof ArmorComponentItem component) {
                String componentId = BuiltInRegistries.ITEM.getKey(componentStack.getItem()).toString();
                boolean added = ArmorFrameItem.addComponent(
                        frameStack,
                        componentId,
                        component.getComponentGroup(),
                        component.getDefenseBonus(),
                        component.getDurabilityBonus(),
                        component.getToughnessBonus()
                );

                if (!added) {
                    hadDuplicate = true;
                }
            }
        }

        if (hadDuplicate && player != null) {
            player.displayClientMessage(Component.literal("\u00A7cCannot add duplicate component types!"), true);
        }

        ArmorFrameAttributeModifiers.updateAttributes(frameStack);
        container.setChanged();
    }

    private void saveComponentsToFrameAndConsume(ItemStack frameStack, Player player) {
        if (!(frameStack.getItem() instanceof ArmorFrameItem)) {
            return;
        }

        CompoundTag nbt = new CompoundTag();
        nbt.put("Components", new ListTag());
        frameStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        for (int i = 0; i < currentLayout.slots().size(); i++) {
            int slotIndex = UPGRADE_SLOTS_START + i;
            ItemStack componentStack = container.getItem(slotIndex);
            if (componentStack.getItem() instanceof ArmorComponentItem component) {
                String componentId = BuiltInRegistries.ITEM.getKey(componentStack.getItem()).toString();
                boolean added = ArmorFrameItem.addComponent(
                        frameStack,
                        componentId,
                        component.getComponentGroup(),
                        component.getDefenseBonus(),
                        component.getDurabilityBonus(),
                        component.getToughnessBonus()
                );

                if (added) {
                    container.setItem(slotIndex, ItemStack.EMPTY);
                } else {
                    ItemStack rejected = componentStack.copy();
                    container.setItem(slotIndex, ItemStack.EMPTY);

                    if (!player.getInventory().add(rejected)) {
                        player.drop(rejected, false);
                    }

                    player.displayClientMessage(Component.literal("\u00A7eComponent rejected: " +
                            component.getComponentType().getDisplayName() +
                            " (duplicate type)"), true);
                }
            }
        }

        ArmorFrameAttributeModifiers.updateAttributes(frameStack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot < INVENTORY_START) {
                if (invSlot == FRAME_SLOT) {
                    saveComponentsToFrameAndConsume(originalStack, player);
                }

                if (!this.moveItemStackTo(originalStack, INVENTORY_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

                if (invSlot == FRAME_SLOT) {
                    updateLayout();
                }
            } else {
                boolean wasInserted;

                if (!this.moveItemStackTo(originalStack, FRAME_SLOT, FRAME_SLOT + 1, false)) {
                    if (this.moveItemStackTo(originalStack, UPGRADE_SLOTS_START, INVENTORY_START, false)) {
                        wasInserted = true;
                        updateFrameStatsPreview(player);
                    } else {
                        wasInserted = false;
                    }
                } else {
                    wasInserted = true;
                    updateLayout();
                    syncFrameWithComponents();
                }

                if (!wasInserted) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        ItemStack frameStack = container.getItem(FRAME_SLOT);

        if (frameStack.getItem() instanceof ArmorFrameItem) {
            saveComponentsToFrameAndConsume(frameStack, player);
        }

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }

    public Container getInventory() {
        return this.container;
    }
}
