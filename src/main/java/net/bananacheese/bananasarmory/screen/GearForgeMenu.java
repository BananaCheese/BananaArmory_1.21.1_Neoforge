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

import java.util.List;

public class GearForgeMenu extends AbstractContainerMenu {
    private final Container container;

    private static final int FRAME_SLOT = 0;
    private static final int UPGRADE_SLOTS_START = 1;
    private static final int UPGRADE_SLOTS_COUNT = 6;
    private static final int INVENTORY_START = UPGRADE_SLOTS_START + UPGRADE_SLOTS_COUNT;

    public GearForgeMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(7));
    }

    public GearForgeMenu(int syncId, Inventory playerInventory, Container container) {
        super(BAScreenHandlers.GEAR_FORGE_MENU.get(), syncId);

        checkContainerSize(container, 7);
        this.container = container;

        this.addSlot(new GearForgeSlot(container, FRAME_SLOT, 113, 38, GearForgeSlot.SlotType.FRAME));

        this.addSlot(new GearForgeSlot(container, 1, 101, 17, GearForgeSlot.SlotType.COMPONENT));
        this.addSlot(new GearForgeSlot(container, 2, 125, 17, GearForgeSlot.SlotType.COMPONENT));
        this.addSlot(new GearForgeSlot(container, 3, 89, 38, GearForgeSlot.SlotType.COMPONENT));
        this.addSlot(new GearForgeSlot(container, 4, 137, 38, GearForgeSlot.SlotType.COMPONENT));
        this.addSlot(new GearForgeSlot(container, 5, 101, 59, GearForgeSlot.SlotType.COMPONENT));
        this.addSlot(new GearForgeSlot(container, 6, 125, 59, GearForgeSlot.SlotType.COMPONENT));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 87 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 145));
        }
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        ItemStack frameBeforeClick = container.getItem(FRAME_SLOT).copy();
        boolean hadFrameBefore = frameBeforeClick.getItem() instanceof ArmorFrameItem;

        super.clicked(slotIndex, button, clickType, player);

        ItemStack frameAfterClick = container.getItem(FRAME_SLOT);
        boolean hasFrameAfter = frameAfterClick.getItem() instanceof ArmorFrameItem;

        if (!hadFrameBefore && hasFrameAfter) {
            syncFrameWithComponents();
        } else if (hadFrameBefore && !hasFrameAfter && slotIndex == FRAME_SLOT) {
            saveComponentsToFrameAndConsume(frameBeforeClick, player);

            for (int i = 1; i <= 6; i++) {
                container.setItem(i, ItemStack.EMPTY);
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

            for (int i = 0; i < 6; i++) {
                if (i < components.size()) {
                    ArmorFrameItem.ComponentData data = components.get(i);
                    ResourceLocation itemId = ResourceLocation.tryParse(data.id());
                    if (itemId != null) {
                        Item item = BuiltInRegistries.ITEM.get(itemId);
                        if (item instanceof ArmorComponentItem) {
                            container.setItem(i + 1, new ItemStack(item, 1));
                        }
                    }
                } else {
                    container.setItem(i + 1, ItemStack.EMPTY);
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
        for (int i = 1; i <= 6; i++) {
            ItemStack componentStack = container.getItem(i);
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

        for (int i = 1; i <= 6; i++) {
            ItemStack componentStack = container.getItem(i);
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
                    container.setItem(i, ItemStack.EMPTY);
                } else {
                    ItemStack rejected = componentStack.copy();
                    container.setItem(i, ItemStack.EMPTY);

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
