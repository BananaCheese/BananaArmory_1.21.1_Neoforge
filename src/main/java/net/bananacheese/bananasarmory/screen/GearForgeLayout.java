package net.bananacheese.bananasarmory.screen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public record GearForgeLayout(ResourceLocation background, List<SlotDef> slots) {

    /** No item in the frame slot (or an unrecognized one) — every upgrade slot inactive. */
    public static GearForgeLayout empty(ResourceLocation defaultBackground) {
        return new GearForgeLayout(defaultBackground, List.of());
    }

    public record SlotDef(String name, int x, int y, Predicate<ItemStack> validItem) {
    }
}
