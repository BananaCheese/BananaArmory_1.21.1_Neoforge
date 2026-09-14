package net.bananacheese.bananasarmory.block.entity;

import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.block.BABlocks;
import net.bananacheese.bananasarmory.block.entity.custom.GearForgeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BABlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BananasArmory.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GearForgeBlockEntity>> GEAR_FORGE_BE =
            BLOCK_ENTITY_TYPES.register("gear_forge_block_entity",
                    () -> BlockEntityType.Builder.of(GearForgeBlockEntity::new, BABlocks.GEAR_FORGE_BLOCK.get()).build(null));

    private BABlockEntities() {
    }
}
