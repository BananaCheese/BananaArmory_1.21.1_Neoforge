package net.bananacheese.bananasarmory.block;

import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.block.custom.GearForgeBlock;
import net.bananacheese.bananasarmory.block.custom.MultiblockDummyBlock;
import net.bananacheese.bananasarmory.item.BAItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BABlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(BananasArmory.MODID);
    //public static final DeferredRegister.Items BLOCK_ITEMS =
    //        DeferredRegister.createItems(BananasArmory.MODID);

    public static final DeferredBlock<Block> GEAR_FORGE_BLOCK = registerBlock("gear_forge",
            () -> new  GearForgeBlock(BlockBehaviour.Properties.of().strength(4.0f).requiresCorrectToolForDrops().sound(SoundType.ANVIL)));

    public static final DeferredBlock<Block> MULTIBLOCK_DUMMY = BLOCKS.register("multiblock_dummy",
            () -> new  MultiblockDummyBlock(BlockBehaviour.Properties.of().strength(-1.0f, 3600000.0f).noLootTable().isValidSpawn((state, level, pos, type) -> false)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        BAItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
