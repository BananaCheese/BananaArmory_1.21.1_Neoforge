package net.bananacheese.bananasarmory.item.custom;

import net.bananacheese.bananasarmory.block.BABlocks;
import net.bananacheese.bananasarmory.block.entity.custom.GearForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GearForgeHammer extends Item {

    public GearForgeHammer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (state.is(BABlocks.GEAR_FORGE_BLOCK.get())) {
            if (!level.isClientSide) {
                if (level.getBlockEntity(pos) instanceof GearForgeBlockEntity forge) {
                    if (forge.isFormed()) {
                        forge.unformMultiblock();
                        player.displayClientMessage(Component.literal("Multiblock unformed"), true);
                        level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    } else {
                        forge.tryFormMultiblock(level, pos);

                        if (forge.isFormed()) {
                            player.displayClientMessage(Component.literal("Multiblock formed!"), true);
                            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
                        } else {
                            player.displayClientMessage(Component.literal("Invalid structure - check pattern"), true);
                            level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5f, 0.8f);
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (state.is(BABlocks.MULTIBLOCK_DUMMY.get())) {
            if (!level.isClientSide) {
                BlockPos masterPos = findMasterBlock(level, pos);
                if (masterPos != null && level.getBlockEntity(masterPos) instanceof GearForgeBlockEntity forge) {
                    forge.unformMultiblock();
                    player.displayClientMessage(Component.literal("Multiblock unformed"), true);
                    level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide) {
            BlockPos forgePos = findNearbyGearForge(level, pos);
            if (forgePos != null && level.getBlockEntity(forgePos) instanceof GearForgeBlockEntity forge) {
                if (!forge.isFormed()) {
                    forge.tryFormMultiblock(level, forgePos);

                    if (forge.isFormed()) {
                        player.displayClientMessage(Component.literal("Multiblock formed!"), true);
                        level.playSound(null, forgePos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    private BlockPos findNearbyGearForge(Level level, BlockPos clickedPos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = clickedPos.offset(x, y, z);
                    if (level.getBlockState(checkPos).is(BABlocks.GEAR_FORGE_BLOCK.get())) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findMasterBlock(Level level, BlockPos dummyPos) {
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = dummyPos.offset(x, y, z);
                    if (level.getBlockEntity(checkPos) instanceof GearForgeBlockEntity forge) {
                        if (forge.isFormed() && forge.isDummyPartOfMultiblock(dummyPos)) {
                            return checkPos;
                        }
                    }
                }
            }
        }
        return null;
    }
}
