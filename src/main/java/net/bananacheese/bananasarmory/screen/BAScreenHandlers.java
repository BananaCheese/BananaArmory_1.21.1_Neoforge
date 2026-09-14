package net.bananacheese.bananasarmory.screen;

import net.bananacheese.bananasarmory.BananasArmory;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BAScreenHandlers {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, BananasArmory.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GearForgeMenu>> GEAR_FORGE_MENU =
            MENUS.register("gear_forge", () -> IMenuTypeExtension.create((containerId, playerInv, extraData) -> new GearForgeMenu(containerId, playerInv)));

    private BAScreenHandlers() {
    }
}
