package net.bananacheese.bananasarmory.screen.slot;

import net.bananacheese.bananasarmory.screen.GearForgeMenu;
import net.bananacheese.bananasarmory.screen.GearForgeable;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GearForgeSlot extends Slot {
    private final SlotRole role;
    private final int upgradeIndex;
    private final GearForgeMenu menu;

    public GearForgeSlot(Container container, int index, int x, int y, SlotRole role,
                         int upgradeIndex, GearForgeMenu menu) {
        super(container, index, x, y);
        this.role = role;
        this.upgradeIndex = upgradeIndex;
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return switch (role) {
            case FRAME -> stack.getItem() instanceof GearForgeable;
            case UPGRADE -> menu.isUpgradeSlotActive(upgradeIndex) && menu.isValidForUpgradeSlot(upgradeIndex, stack);
        };
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public enum SlotRole {
        FRAME,
        UPGRADE
    }
}
