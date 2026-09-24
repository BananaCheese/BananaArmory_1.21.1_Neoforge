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

public class GearForgeBlockEntity extends BlockEntity implements Container {
    private boolean isFormed = false;
    private final Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
    private int checkCooldown = 0;
    private static final int CHECK_INTERVAL = 40;

    // 1 frame slot + GearForgeMenu.MAX_UPGRADE_SLOTS (8) — kept as a literal
    // here rather than importing the constant, since block entity classes
    // shouldn't depend on the (client/menu-focused) screen package. Keep
    // this in sync with GearForgeMenu.MAX_UPGRADE_SLOTS if that ever changes.
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);

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

        // These aren't part of the required pattern (any block, or air, is
        // fine here) but they ARE inside the big formed model's visual
        // footprint. Without converting them to MULTIBLOCK_DUMMY too, they
        // just keep whatever was there before — usually air — so the
        // structure looks solid but isn't: players can walk straight
        // through these spots even though the model visually covers them.
        BlockPos[] topFillerCells = {
                controllerPos.offset(0, 1, 0),  // top-center, directly above controller
                controllerPos.offset(-1, 1, 0),
                controllerPos.offset(1, 1, 0),
                controllerPos.offset(0, 1, -1),
                controllerPos.offset(0, 1, 1)
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

        if (structureValid) {
            // Capture the filler cells too so they get swapped to real,
            // solid MULTIBLOCK_DUMMY blocks — no type requirement, whatever
            // is there (including air) gets absorbed and restored on unform.
            for (BlockPos fillerPos : topFillerCells) {
                BlockState state = level.getBlockState(fillerPos);
                tempOriginalBlocks.put(fillerPos.immutable(), state);
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