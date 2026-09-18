package net.bananacheese.bananasarmory.item;

import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.block.BABlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BAItemGroup {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BananasArmory.MODID);

    public static final Supplier<CreativeModeTab> BARMORY = CREATIVE_MODE_TABS.register("bananasarmory",
            () -> CreativeModeTab.builder().title(Component.translatable("creativetab.barmory")).icon(() -> new ItemStack(BAItems.GEAR_FORGE_HAMMER.get())).displayItems((pParameters, pOutput) -> {

                pOutput.accept(BABlocks.GEAR_FORGE_BLOCK);

                pOutput.accept(BAItems.GEAR_FORGE_HAMMER);

                pOutput.accept(BAItems.HELMET_FRAME);
                pOutput.accept(BAItems.CHESTPLATE_FRAME);
                pOutput.accept(BAItems.LEGGINGS_FRAME);
                pOutput.accept(BAItems.BOOTS_FRAME);

                pOutput.accept(BAItems.IRON_PAULDRONS);
                pOutput.accept(BAItems.IRON_FAULD);
                pOutput.accept(BAItems.IRON_GORGET);
                pOutput.accept(BAItems.IRON_REINFORCEMENT);

                pOutput.accept(BAItems.DIAMOND_PAULDRONS);
            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
