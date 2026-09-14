package net.bananacheese.bananasarmory.screen.slot;

import net.bananacheese.bananasarmory.item.custom.ArmorComponentItem;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GearForgeSlot extends Slot {
    private final SlotType slotType;
    private final Container container;

    public GearForgeSlot(Container container, int index, int x, int y, SlotType slotType) {
        super(container, index, x, y);
        this.slotType = slotType;
        this.container = container;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return switch (slotType) {
            case FRAME -> stack.getItem() instanceof ArmorFrameItem;
            case COMPONENT -> {
                ItemStack frameStack = container.getItem(0);
                if (!(frameStack.getItem() instanceof ArmorFrameItem frameItem)) {
                    yield false;
                }

                if (stack.getItem() instanceof ArmorComponentItem component) {
                    yield component.getComponentType().isCompatibleWith(frameItem.getFrameType());
                }
                yield false;
            }
        };
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public enum SlotType {
        FRAME,
        COMPONENT
    }
}
