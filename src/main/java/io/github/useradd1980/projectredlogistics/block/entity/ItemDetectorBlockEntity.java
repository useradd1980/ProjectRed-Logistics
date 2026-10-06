package io.github.useradd1980.projectredlogistics.block.entity;

import codechicken.lib.inventory.container.CCLMenuType;
import codechicken.lib.vec.Vector3;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import io.github.useradd1980.projectredlogistics.menu.ItemDetectorMenu;
import mrtjp.projectred.core.inventory.BaseContainer;
import mrtjp.projectred.expansion.part.PneumaticTubePayload;
import mrtjp.projectred.expansion.pneumatics.PneumaticTransportMode;
import mrtjp.projectred.expansion.tile.BasePneumaticDeviceBlockEntity;
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
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

public class ItemDetectorBlockEntity extends BasePneumaticDeviceBlockEntity {

    private static final String TAG_FILTER = "filter";
    private static final String TAG_MODE = "mode";

    public static final int FILTER_SIZE = 9;
    public static final int MODE_ITEMS = 0;
    public static final int MODE_STACKS = 1;
    public static final int MODE_JAM = 2;

    private final BaseContainer filterInventory = new BaseContainer(FILTER_SIZE);
    private final IItemHandler filterItemHandler = new InvWrapper(filterInventory);

    private int mode = MODE_ITEMS;

    public ItemDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(LogisticsContent.ITEM_DETECTOR_BLOCK_ENTITY.get(), pos, state);
        filterInventory.addListener(container -> setChanged());
    }

    @Override
    public void saveToNBT(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.saveToNBT(tag, lookupProvider);
        filterInventory.saveTo(tag, TAG_FILTER, lookupProvider);
        tag.putByte(TAG_MODE, (byte) mode);
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.loadFromNBT(tag, lookupProvider);
        filterInventory.loadFrom(tag, TAG_FILTER, lookupProvider);
        mode = Math.max(MODE_ITEMS, Math.min(MODE_JAM, tag.getByte(TAG_MODE)));
    }

    @Override
    public void onBlockRemoved() {
        super.onBlockRemoved();
        dropInventory(filterInventory, getLevel(), Vector3.fromBlockPos(getBlockPos()));
    }

    @Override
    public boolean canAcceptPayload(
            int s,
            PneumaticTubePayload payload,
            PneumaticTransportMode transportMode) {

        return switch (transportMode) {
            case PASSIVE_NORMAL -> s == (side ^ 1) && itemQueue.isEmpty();
            case PASSIVE_BACKSTUFF -> s == side;
        };
    }

    @Override
    protected void onActivated() {
    }

    @Override
    protected void onDeactivated() {
    }

    @Override
    public ItemInteractionResult useItemOn(
            ItemStack held,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {

        ItemInteractionResult parent = super.useItemOn(held, player, hand, hit);
        if (parent.consumesAction()) {
            return parent;
        }

        openMenu(player);
        return ItemInteractionResult.sidedSuccess(getLevel().isClientSide());
    }

    @Override
    public InteractionResult useWithoutItem(Player player, BlockHitResult hit) {
        openMenu(player);
        return InteractionResult.sidedSuccess(getLevel().isClientSide());
    }

    private void openMenu(Player player) {
        if (getLevel().isClientSide()) return;

        CCLMenuType.openMenu(
                (ServerPlayer) player,
                new SimpleMenuProvider(
                        (id, inventory, p) ->
                                new ItemDetectorMenu(inventory, this, id),
                        getBlockState().getBlock().getName()),
                packet -> packet.writePos(getBlockPos()));
    }

    public BaseContainer getFilterInventory() {
        return filterInventory;
    }

    public @Nullable IItemHandler getFilterItemHandler(@Nullable Direction direction) {
        if (direction == null) {
            return filterItemHandler;
        }

        int s = direction.ordinal();
        return s == side || s == (side ^ 1) ? null : filterItemHandler;
    }

    public int getMode() {
        return mode;
    }

    public void cycleMode(boolean forward) {
        int next = mode + (forward ? 1 : -1);
        if (next > MODE_JAM) next = MODE_ITEMS;
        if (next < MODE_ITEMS) next = MODE_JAM;
        setMode(next);
    }

    public void setMode(int mode) {
        if (mode < MODE_ITEMS || mode > MODE_JAM) {
            throw new IllegalArgumentException(
                    "Item Detector mode must be 0..2: " + mode);
        }

        this.mode = mode;
        setChanged();
    }
}
