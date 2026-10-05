package io.github.useradd1980.projectredlogistics.block.entity;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.inventory.container.CCLMenuType;
import codechicken.lib.vec.Vector3;
import io.github.useradd1980.projectredlogistics.buffer.BufferSlotMap;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import io.github.useradd1980.projectredlogistics.menu.BufferMenu;
import mrtjp.projectred.api.IScrewdriver;
import mrtjp.projectred.core.block.ProjectRedBlock;
import mrtjp.projectred.core.inventory.BaseContainer;
import mrtjp.projectred.core.tile.ProjectRedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class BufferBlockEntity extends ProjectRedBlockEntity {

    private static final String TAG_INVENTORY = "buffer_inventory";

    public static final int INVENTORY_SIZE = BufferSlotMap.INVENTORY_SIZE;

    private final BaseContainer inventory = new BaseContainer(INVENTORY_SIZE);
    private final IItemHandler unsidedHandler = new BufferItemHandler(null);
    private final IItemHandler[] sidedHandlers = new IItemHandler[6];

    public BufferBlockEntity(BlockPos pos, BlockState state) {
        super(LogisticsContent.BUFFER_BLOCK_ENTITY.get(), pos, state);
        inventory.addListener(container -> setChanged());

        for (Direction direction : Direction.values()) {
            sidedHandlers[direction.ordinal()] = new BufferItemHandler(direction);
        }
    }

    @Override
    public void saveToNBT(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        inventory.saveTo(tag, TAG_INVENTORY, lookupProvider);
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        inventory.loadFrom(tag, TAG_INVENTORY, lookupProvider);
    }

    @Override
    public void writeDesc(MCDataOutput out) {
    }

    @Override
    public void readDesc(MCDataInput in) {
    }

    @Override
    public void onBlockRemoved() {
        super.onBlockRemoved();
        dropInventory(inventory, getBlockLevel(), Vector3.fromBlockPos(getBlockPos()));
    }

    @Override
    public ItemInteractionResult useItemOn(
            ItemStack held,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {

        if (held.getItem() instanceof IScrewdriver screwdriver) {
            if (!screwdriver.canUse(player, hand)) {
                return ItemInteractionResult.FAIL;
            }

            if (!getBlockLevel().isClientSide()) {
                BlockState state = getBlockState();
                int nextSide = (state.getValue(ProjectRedBlock.SIDE) + 1) % 6;

                getBlockLevel().setBlockAndUpdate(
                        getBlockPos(),
                        state.setValue(ProjectRedBlock.SIDE, nextSide));

                screwdriver.damageScrewdriver(player, hand);
                setChanged();
            }

            return ItemInteractionResult.sidedSuccess(getBlockLevel().isClientSide());
        }

        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        openMenu(player);
        return ItemInteractionResult.sidedSuccess(getBlockLevel().isClientSide());
    }

    @Override
    public InteractionResult useWithoutItem(Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        openMenu(player);
        return InteractionResult.sidedSuccess(getBlockLevel().isClientSide());
    }

    private void openMenu(Player player) {
        if (getBlockLevel().isClientSide()) return;

        CCLMenuType.openMenu(
                (ServerPlayer) player,
                new SimpleMenuProvider(
                        (id, playerInventory, p) ->
                                new BufferMenu(playerInventory, this, id),
                        getBlockState().getBlock().getName()),
                packet -> packet.writePos(getBlockPos()));
    }

    public BaseContainer getInventory() {
        return inventory;
    }

    public IItemHandler getItemHandler(@Nullable Direction direction) {
        return direction == null
                ? unsidedHandler
                : sidedHandlers[direction.ordinal()];
    }

    private int getFrontSide() {
        return getBlockState().getValue(ProjectRedBlock.SIDE);
    }

    private final class BufferItemHandler implements IItemHandler {

        private final @Nullable Direction accessSide;

        private BufferItemHandler(@Nullable Direction accessSide) {
            this.accessSide = accessSide;
        }

        @Override
        public int getSlots() {
            return accessSide == null
                    ? INVENTORY_SIZE
                    : BufferSlotMap.slotCount(getFrontSide(), accessSide.ordinal());
        }

        private int mapSlot(int slot) {
            if (accessSide == null) {
                if (slot < 0 || slot >= INVENTORY_SIZE) {
                    throw new IndexOutOfBoundsException("Buffer slot " + slot);
                }
                return slot;
            }

            return BufferSlotMap.mapSlot(
                    getFrontSide(),
                    accessSide.ordinal(),
                    slot);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getItem(mapSlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;

            int actualSlot = mapSlot(slot);
            if (!inventory.canPlaceItem(actualSlot, stack)) return stack;

            ItemStack existing = inventory.getItem(actualSlot);

            if (!existing.isEmpty()
                    && !ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }

            int currentCount = existing.isEmpty() ? 0 : existing.getCount();
            int limit = stack.getMaxStackSize();
            int move = Math.min(
                    stack.getCount(),
                    Math.max(0, limit - currentCount));

            if (move <= 0) return stack;

            if (!simulate) {
                ItemStack updated = existing.isEmpty()
                        ? stack.copy()
                        : existing.copy();

                updated.setCount(currentCount + move);
                inventory.setItem(actualSlot, updated);
            }

            if (move == stack.getCount()) return ItemStack.EMPTY;

            ItemStack remainder = stack.copy();
            remainder.shrink(move);
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) return ItemStack.EMPTY;

            int actualSlot = mapSlot(slot);
            ItemStack existing = inventory.getItem(actualSlot);
            if (existing.isEmpty()) return ItemStack.EMPTY;

            int take = Math.min(amount, existing.getCount());

            if (simulate) {
                ItemStack result = existing.copy();
                result.setCount(take);
                return result;
            }

            return inventory.removeItem(actualSlot, take);
        }

        @Override
        public int getSlotLimit(int slot) {
            mapSlot(slot);
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return inventory.canPlaceItem(mapSlot(slot), stack);
        }
    }
}
