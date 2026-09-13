package net.bananacheese.bananasarmory.block.entity.custom;

import net.bananacheese.bananasarmory.block.BABlocks;
import net.bananacheese.bananasarmory.block.entity.BABlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ported from GearForgeBlockEntity.
 *
 * Renames: Inventory -> Container, Inventories -> ContainerHelper,
 * DefaultedList -> NonNullList, PlayerEntity -> Player,
 * markDirty -> setChanged, canPlayerUse -> stillValid,
 * getStack/setStack/removeStack -> getItem/setItem/removeItem(NoUpdate),
 * getMaxCountPerStack -> getMaxStackSize, Identifier -> ResourceLocation,
 * BlockEntityUpdateS2CPacket -> ClientboundBlockEntityDataPacket,
 * toInitialChunkDataNbt -> getUpdateTag.
 *
 * IMPORTANT VERSION NOTE: the original (1.21.8) uses the newer ReadView/
 * WriteView serialization API. 1.21.1 predates that — this port uses the
 * older loadAdditional(CompoundTag, HolderLookup.Provider) /
 * saveAdditional(CompoundTag, HolderLookup.Provider) pair instead, storing
 * the same logical data (formed flag, sparse inventory snapshot, original
 * block map) directly as NBT tags/lists rather than indexed keys, since
 * CompoundTag makes list storage easier than the ReadView helper did.
 */
public class GearForgeBlockEntity extends BlockEntity implements Container {
    private boolean isFormed = false;
    private final Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
    private int checkCooldown = 0;
    private static final int CHECK_INTERVAL = 40;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);

    public GearForgeBlockEntity(BlockPos pos, BlockState state) {
        super(BABlockEntities.GEAR_FORGE_BE.get(), pos, state);
    }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        checkCooldown--;
        if (checkCooldown <= 0 && isFormed) {
            checkCooldown = CHECK_INTERVAL;

            if (!verifyStructure(level)) {
                unformMultiblock();
                setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            }
        }
    }

    public void tryFormMultiblock(Level level, BlockPos pos) {
        if (!isFormed) {
            checkAndFormMultiblock(level, pos);
            if (isFormed) {
                setChanged();
                BlockState state = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            }
        }
    }

    private boolean verifyStructure(Level level) {
        for (BlockPos dummyPos : originalBlocks.keySet()) {
            if (!level.getBlockState(dummyPos).is(BABlocks.MULTIBLOCK_DUMMY.get())) {
                return false;
            }
        }
        return true;
    }

    private void checkAndFormMultiblock(Level level, BlockPos controllerPos) {
        BlockPos[] bottomCorners = {
                controllerPos.offset(-1, 0, -1),
                controllerPos.offset(1, 0, -1),
                controllerPos.offset(-1, 0, 1),
                controllerPos.offset(1, 0, 1)
        };

        BlockPos[] bottomSides = {
                controllerPos.offset(-1, 0, 0),
                controllerPos.offset(1, 0, 0),
                controllerPos.offset(0, 0, -1),
                controllerPos.offset(0, 0, 1)
        };

        BlockPos[] topCorners = {
                controllerPos.offset(1, 1, 1),
                controllerPos.offset(1, 1, -1),
                controllerPos.offset(-1, 1, 1),
                controllerPos.offset(-1, 1, -1)
        };

        Block[] requiredTopBlocks = {
                Blocks.ANVIL,
                Blocks.GRINDSTONE,
                Blocks.SMITHING_TABLE,
                Blocks.BLAST_FURNACE
        };

        boolean structureValid = true;
        Map<BlockPos, BlockState> tempOriginalBlocks = new HashMap<>();

        for (BlockPos cornerPos : bottomCorners) {
            BlockState state = level.getBlockState(cornerPos);
            if (!state.is(Blocks.CRYING_OBSIDIAN)) {
                structureValid = false;
                break;
            }
            tempOriginalBlocks.put(cornerPos.immutable(), state);
        }

        if (structureValid) {
            for (BlockPos sidePos : bottomSides) {
                BlockState state = level.getBlockState(sidePos);
                if (!state.is(Blocks.SMOOTH_STONE)) {
                    structureValid = false;
                    break;
                }
                tempOriginalBlocks.put(sidePos.immutable(), state);
            }
        }

        if (structureValid) {
            List<Block> foundBlocks = new ArrayList<>();
            for (BlockPos cornerPos : topCorners) {
                BlockState state = level.getBlockState(cornerPos);
                Block block = state.getBlock();
                foundBlocks.add(block);
                tempOriginalBlocks.put(cornerPos.immutable(), state);
            }

            for (Block requiredBlock : requiredTopBlocks) {
                if (!foundBlocks.contains(requiredBlock)) {
                    structureValid = false;
                    break;
                }
            }
        }

        if (structureValid && !isFormed) {
            isFormed = true;
            originalBlocks.clear();
            originalBlocks.putAll(tempOriginalBlocks);

            for (BlockPos targetPos : tempOriginalBlocks.keySet()) {
                level.setBlock(targetPos, BABlocks.MULTIBLOCK_DUMMY.get().defaultBlockState(), Block.UPDATE_ALL);
            }

            setChanged();
        }
    }

    public void unformMultiblock() {
        if (isFormed && level != null) {
            isFormed = false;

            for (Map.Entry<BlockPos, BlockState> entry : originalBlocks.entrySet()) {
                BlockPos targetPos = entry.getKey();
                BlockState originalState = entry.getValue();

                if (level.getBlockState(targetPos).is(BABlocks.MULTIBLOCK_DUMMY.get())) {
                    level.setBlock(targetPos, originalState, Block.UPDATE_ALL);
                }
            }

            originalBlocks.clear();
            setChanged();
        }
    }

    public boolean isFormed() {
        return isFormed;
    }

    public boolean isDummyPartOfMultiblock(BlockPos dummyPos) {
        return originalBlocks.containsKey(dummyPos);
    }

    public BlockState getOriginalBlock(BlockPos pos) {
        return originalBlocks.get(pos);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        isFormed = tag.getBoolean("IsFormed");

        inventory.clear();
        ContainerHelper.loadAllItems(tag, inventory, registries);

        originalBlocks.clear();
        if (tag.contains("OriginalBlocksCount")) {
            int count = tag.getInt("OriginalBlocksCount");
            for (int i = 0; i < count; i++) {
                long posLong = tag.getLong("BlockPos" + i);
                String blockId = tag.getString("BlockId" + i);
                if (posLong != 0) {
                    BlockPos pos = BlockPos.of(posLong);
                    Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
                    originalBlocks.put(pos, block.defaultBlockState());
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("IsFormed", isFormed);

        ContainerHelper.saveAllItems(tag, inventory, registries);

        tag.putInt("OriginalBlocksCount", originalBlocks.size());
        int index = 0;
        for (Map.Entry<BlockPos, BlockState> entry : originalBlocks.entrySet()) {
            tag.putLong("BlockPos" + index, entry.getKey().asLong());
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlock());
            tag.putString("BlockId" + index, blockId.toString());
            index++;
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
