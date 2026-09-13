package net.bananacheese.bananasarmory.screen;

import net.bananacheese.bananasarmory.BananasArmory;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Ported from DAScreenHandlers. NeoForge's MenuType registration is
 * essentially the same shape as vanilla's ScreenHandlerType, just registered
 * through DeferredRegister instead of a raw Registry.register call.
 *
 * NOTE: `IMenuTypeExtension.create(...)` is NeoForge's helper for
 * data-carrying-free menus opened via `player.openMenu(MenuProvider)` (which
 * is how GearForgeBlock opens this one) — double check this exact helper
 * name against your NeoForge version if the menu doesn't open; the older
 * pattern is `new MenuType<>(GearForgeMenu::new, FeatureFlags.VANILLA_SET)`.
 */
public final class BAScreenHandlers {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, BananasArmory.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GearForgeMenu>> GEAR_FORGE_MENU =
            MENUS.register("gear_forge", () -> IMenuTypeExtension.create((containerId, playerInv, extraData) -> new GearForgeMenu(containerId, playerInv)));

    private BAScreenHandlers() {
    }
}
