package io.github.useradd1980.projectredlogistics.block.entity;

import codechicken.lib.inventory.container.CCLMenuType;
import codechicken.lib.vec.Vector3;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import io.github.useradd1980.projectredlogistics.menu.FilterMenu;
import io.github.useradd1980.projectredlogistics.routing.LogisticsRoutingData;
import mrtjp.projectred.core.CenterLookup;
import mrtjp.projectred.core.inventory.BaseContainer;
import mrtjp.projectred.expansion.part.PneumaticTubePayload;
import mrtjp.projectred.expansion.tile.BasePneumaticDeviceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.Objects;

/**
 * RP2-style Filter.
 *
 * The nine filter slots are real inventory slots. Their stack counts are part
 * of the filter rule: a stack of 16 cobblestone requests a batch of 16.
 *
 * An entirely empty filter extracts the first complete source stack.
 */
public class FilterBlockEntity extends BasePneumaticDeviceBlockEntity {

    private static final String TAG_FILTER = "filter";
    private static final String TAG_ROUTE_COLOUR = "route_colour";

    public static final int FILTER_SIZE = 9;
    public static final int NO_COLOUR = -1;

    private final BaseContainer filterInventory = new BaseContainer(FILTER_SIZE);
    private final IItemHandler filterItemHandler = new InvWrapper(filterInventory);

    private int routeColour = NO_COLOUR;

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(LogisticsContent.FILTER_BLOCK_ENTITY.get(), pos, state);
        filterInventory.addListener(container -> setChanged());
    }

    //region persistence
    @Override
    public void saveToNBT(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.saveToNBT(tag, lookupProvider);
        filterInventory.saveTo(tag, TAG_FILTER, lookupProvider);
        tag.putInt(TAG_ROUTE_COLOUR, routeColour);
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.loadFromNBT(tag, lookupProvider);
        filterInventory.loadFrom(tag, TAG_FILTER, lookupProvider);
        routeColour = tag.contains(TAG_ROUTE_COLOUR)
                ? tag.getInt(TAG_ROUTE_COLOUR)
                : NO_COLOUR;
    }
    //endregion

    @Override
    public void onBlockRemoved() {
        super.onBlockRemoved();
        dropInventory(
                filterInventory,
                getLevel(),
                Vector3.fromBlockPos(getBlockPos()));
    }

    @Override
    protected void onActivated() {
        ItemStack extracted = extractFromRearInventory();
        if (extracted.isEmpty()) return;

        PneumaticTubePayload payload = new PneumaticTubePayload(extracted);
        if (routeColour >= 0) {
            LogisticsRoutingData.setPayloadColour(payload, routeColour);
        }

        itemQueue.add(payload);

        // Unlike the current ProjectRed Transposer, attempt to hand the batch
        // to the tube immediately. Backstuff handling remains inherited from
        // BasePneumaticDeviceBlockEntity.
        exportQueue();
        scheduleTick(4);
    }

    @Override
    protected void onDeactivated() {
    }

    /**
     * Temporary pre-GUI controls:
     * - dye: set output route colour
     * - sneak + empty hand: clear output route colour
     */
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

        if (held.getItem() instanceof DyeItem dye) {
            if (!getLevel().isClientSide()) {
                routeColour = dye.getDyeColor().getId();
                setChanged();
                player.displayClientMessage(
                        Component.literal(
                                "Filter output colour: "
                                        + dye.getDyeColor().getName()),
                        false);
            }
            return ItemInteractionResult.sidedSuccess(getLevel().isClientSide());
        }

        if (held.isEmpty() && player.isShiftKeyDown() && routeColour != NO_COLOUR) {
            if (!getLevel().isClientSide()) {
                routeColour = NO_COLOUR;
                setChanged();
                player.displayClientMessage(
                        Component.literal("Filter output colour cleared"),
                        false);
            }
            return ItemInteractionResult.sidedSuccess(getLevel().isClientSide());
        }

        // Normal right-click opens the Filter inventory.
        if (!getLevel().isClientSide()) {
            CCLMenuType.openMenu(
                    (ServerPlayer) player,
                    new SimpleMenuProvider(
                            (id, inventory, p) -> new FilterMenu(inventory, this, id),
                            getBlockState().getBlock().getName()),
                    packet -> packet.writePos(getBlockPos()));
        }
        return ItemInteractionResult.sidedSuccess(getLevel().isClientSide());
    }

    private ItemStack extractFromRearInventory() {
        CenterLookup lookup = CenterLookup.lookupStraightCenter(
                getLevel(),
                getBlockPos(),
                side ^ 1);

        if (lookup.tile == null) return ItemStack.EMPTY;

        Direction extractDirection = Direction.values()[lookup.otherDirection];

        // Match ProjectRed's Transposer behaviour for vanilla/worldly
        // containers first.
        if (lookup.tile instanceof WorldlyContainer container) {
            return extractFromWorldlyContainer(container, extractDirection);
        }

        var level = Objects.requireNonNull(lookup.tile.getLevel());
        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK,
                lookup.tile.getBlockPos(),
                extractDirection);

        return handler == null
                ? ItemStack.EMPTY
                : extractFromItemHandler(handler);
    }

    private ItemStack extractFromWorldlyContainer(
            WorldlyContainer source,
            Direction extractDirection) {

        int[] slots = source.getSlotsForFace(extractDirection);

        if (isFilterEmpty()) {
            for (int slot : slots) {
                ItemStack stack = source.getItem(slot);
                if (stack.isEmpty()) continue;
                if (!source.canTakeItemThroughFace(
                        slot, stack, extractDirection)) continue;

                return source.removeItem(slot, stack.getCount());
            }
            return ItemStack.EMPTY;
        }

        for (int filterSlot = 0; filterSlot < FILTER_SIZE; filterSlot++) {
            ItemStack template = filterInventory.getItem(filterSlot);
            if (template.isEmpty()) continue;

            int wanted = template.getCount();
            int available = 0;

            for (int slot : slots) {
                ItemStack stack = source.getItem(slot);
                if (!matches(template, stack)) continue;
                if (!source.canTakeItemThroughFace(
                        slot, stack, extractDirection)) continue;

                available += stack.getCount();
                if (available >= wanted) break;
            }

            if (available < wanted) continue;

            ItemStack result = ItemStack.EMPTY;
            int remaining = wanted;

            for (int slot : slots) {
                if (remaining <= 0) break;

                ItemStack stack = source.getItem(slot);
                if (!matches(template, stack)) continue;
                if (!source.canTakeItemThroughFace(
                        slot, stack, extractDirection)) continue;

                ItemStack removed = source.removeItem(
                        slot,
                        Math.min(remaining, stack.getCount()));
                if (removed.isEmpty()) continue;

                if (result.isEmpty()) {
                    result = removed.copy();
                } else {
                    result.grow(removed.getCount());
                }
                remaining -= removed.getCount();
            }

            if (remaining == 0) return result;
        }

        return ItemStack.EMPTY;
    }

    private ItemStack extractFromItemHandler(IItemHandler source) {
        if (isFilterEmpty()) {
            for (int slot = 0; slot < source.getSlots(); slot++) {
                ItemStack stack = source.getStackInSlot(slot);
                if (stack.isEmpty()) continue;

                ItemStack simulated =
                        source.extractItem(slot, stack.getCount(), true);
                if (simulated.isEmpty()) continue;

                return source.extractItem(
                        slot,
                        simulated.getCount(),
                        false);
            }
            return ItemStack.EMPTY;
        }

        for (int filterSlot = 0; filterSlot < FILTER_SIZE; filterSlot++) {
            ItemStack template = filterInventory.getItem(filterSlot);
            if (template.isEmpty()) continue;

            int wanted = template.getCount();
            int available = 0;

            for (int slot = 0; slot < source.getSlots(); slot++) {
                ItemStack stack = source.getStackInSlot(slot);
                if (!matches(template, stack)) continue;

                ItemStack simulated =
                        source.extractItem(slot, wanted - available, true);
                available += simulated.getCount();

                if (available >= wanted) break;
            }

            if (available < wanted) continue;

            ItemStack result = ItemStack.EMPTY;
            int remaining = wanted;

            for (int slot = 0; slot < source.getSlots() && remaining > 0; slot++) {
                ItemStack stack = source.getStackInSlot(slot);
                if (!matches(template, stack)) continue;

                ItemStack removed =
                        source.extractItem(slot, remaining, false);
                if (removed.isEmpty()) continue;

                if (result.isEmpty()) {
                    result = removed.copy();
                } else {
                    result.grow(removed.getCount());
                }
                remaining -= removed.getCount();
            }

            if (remaining == 0) return result;
        }

        return ItemStack.EMPTY;
    }

    private boolean isFilterEmpty() {
        for (int slot = 0; slot < FILTER_SIZE; slot++) {
            if (!filterInventory.getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean matches(ItemStack template, ItemStack stack) {
        return !stack.isEmpty()
                && ItemStack.isSameItemSameComponents(template, stack);
    }

    public BaseContainer getFilterInventory() {
        return filterInventory;
    }

    public IItemHandler getFilterItemHandler() {
        return filterItemHandler;
    }

    public int getRouteColour() {
        return routeColour;
    }

    public void setRouteColour(int routeColour) {
        if (routeColour < NO_COLOUR || routeColour > 15) {
            throw new IllegalArgumentException(
                    "Filter colour must be -1 or 0..15: " + routeColour);
        }
        this.routeColour = routeColour;
        setChanged();
    }
}
