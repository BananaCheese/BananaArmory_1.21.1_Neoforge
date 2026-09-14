package net.bananacheese.bananasarmory.block.custom;

import net.bananacheese.bananasarmory.block.entity.custom.GearForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.RandomSource;

public class MultiblockDummyBlock extends Block {

    public MultiblockDummyBlock(Properties properties) {
        // Unbreakable by hand — the Gear Forge Hammer handles forming/unforming.
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockPos masterPos = findMasterBlock(level, pos);
            if (masterPos != null) {
                BlockState masterState = level.getBlockState(masterPos);
                BlockHitResult newHit = new BlockHitResult(
                        hit.getLocation(),
                        hit.getDirection(),
                        masterPos,
                        hit.isInside()
                );
                return masterState.useWithoutItem(level, player, newHit);
            }
        }
        return InteractionResult.SUCCESS;
    }

    protected void onRemove(BlockState state, ServerLevel level, BlockPos pos,
                             BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    private BlockPos findMasterBlock(Level level, BlockPos dummyPos) {
        // Search a 5x5x5 area for the master (controller) block.
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

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Prevent particles.
    }
}
