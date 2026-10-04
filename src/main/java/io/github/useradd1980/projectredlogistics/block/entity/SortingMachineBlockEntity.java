package io.github.useradd1980.projectredlogistics.block.entity;

import codechicken.lib.inventory.container.CCLMenuType;
import codechicken.lib.vec.Vector3;
import io.github.useradd1980.projectredlogistics.filter.FilterRules;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import io.github.useradd1980.projectredlogistics.menu.SortingMachineMenu;
import io.github.useradd1980.projectredlogistics.power.PoweredPneumaticDeviceBlockEntity;
import io.github.useradd1980.projectredlogistics.sorting.SortingMachineRules;
import mrtjp.projectred.core.CenterLookup;
import mrtjp.projectred.core.inventory.BaseContainer;
import mrtjp.projectred.expansion.part.PneumaticTubePayload;
import mrtjp.projectred.expansion.pneumatics.PneumaticTransportMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * RedPower 2 pr6-style Sorting Machine.
 *
 * Configuration inventory layout is 5 rows x 8 columns (40 real stacks).
 * Each column has its own routing colour.
 */
public class SortingMachineBlockEntity
        extends PoweredPneumaticDeviceBlockEntity {

    public static final int ROWS = 5;
    public static final int COLUMNS = 8;
    public static final int FILTER_SIZE = ROWS * COLUMNS;

    public static final int MODE_ANYSTACK_SEQUENTIAL =
            SortingMachineRules.MODE_ANYSTACK_SEQUENTIAL;
    public static final int MODE_ALLSTACK_SEQUENTIAL =
            SortingMachineRules.MODE_ALLSTACK_SEQUENTIAL;
    public static final int MODE_RANDOM_ALLSTACK =
            SortingMachineRules.MODE_RANDOM_ALLSTACK;
    public static final int MODE_ANY_ITEM =
            SortingMachineRules.MODE_ANY_ITEM;
    public static final int MODE_ANY_ITEM_DEFAULT =
            SortingMachineRules.MODE_ANY_ITEM_DEFAULT;
    public static final int MODE_ANY_ITEM_WHOLE_STACK =
            SortingMachineRules.MODE_ANY_ITEM_WHOLE_STACK;
    public static final int MODE_WHOLE_STACK_DEFAULT =
            SortingMachineRules.MODE_WHOLE_STACK_DEFAULT;

    public static final int PULL_SINGLE_STEP = 0;
    public static final int PULL_AUTOMATIC = 1;
    public static final int PULL_SINGLE_SWEEP = 2;

    private static final String TAG_FILTER = "filter";
    private static final String TAG_COLOURS = "colours";
    private static final String TAG_MODE = "mode";
    private static final String TAG_PULL_MODE = "pull_mode";
    private static final String TAG_DEFAULT_COLOUR = "default_colour";
    private static final String TAG_COLUMN = "column";
    private static final String TAG_PENDING_SWEEPS = "pending_sweeps";

    private final BaseContainer filterInventory = new BaseContainer(FILTER_SIZE);
    private final int[] columnColours = new int[COLUMNS];

    private int mode = MODE_ANYSTACK_SEQUENTIAL;
    private int pullMode = PULL_SINGLE_STEP;
    private int defaultColour = FilterRules.NO_COLOUR;
    private int currentColumn = 0;
    private int pendingSweeps = 0;

    public SortingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(LogisticsContent.SORTING_MACHINE_BLOCK_ENTITY.get(), pos, state);
        Arrays.fill(columnColours, FilterRules.NO_COLOUR);
        filterInventory.addListener(container -> setChanged());
    }

    @Override
    public void saveToNBT(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.saveToNBT(tag, lookupProvider);
        filterInventory.saveTo(tag, TAG_FILTER, lookupProvider);
        tag.putIntArray(TAG_COLOURS, columnColours);
        tag.putInt(TAG_MODE, mode);
        tag.putInt(TAG_PULL_MODE, pullMode);
        tag.putInt(TAG_DEFAULT_COLOUR, defaultColour);
        tag.putInt(TAG_COLUMN, currentColumn);
        tag.putInt(TAG_PENDING_SWEEPS, pendingSweeps);
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.loadFromNBT(tag, lookupProvider);
        filterInventory.loadFrom(tag, TAG_FILTER, lookupProvider);

        Arrays.fill(columnColours, FilterRules.NO_COLOUR);
        int[] savedColours = tag.getIntArray(TAG_COLOURS);
        for (int i = 0; i < Math.min(savedColours.length, COLUMNS); i++) {
            columnColours[i] = sanitizeColour(savedColours[i]);
        }

        mode = clamp(tag.getInt(TAG_MODE), 0, 6);
        pullMode = clamp(tag.getInt(TAG_PULL_MODE), 0, 2);
        defaultColour = sanitizeColour(tag.contains(TAG_DEFAULT_COLOUR)
                ? tag.getInt(TAG_DEFAULT_COLOUR)
                : FilterRules.NO_COLOUR);
        currentColumn = clamp(tag.getInt(TAG_COLUMN), 0, COLUMNS - 1);
        pendingSweeps = Math.max(0, tag.getInt(TAG_PENDING_SWEEPS));
    }

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
        switch (pullMode) {
            case PULL_SINGLE_STEP -> runPullStepAndExport();
            case PULL_SINGLE_SWEEP -> {
                pendingSweeps++;
                setChanged();
                runSweepStep();
            }
            default -> {
                // Automatic mode is driven from tick().
            }
        }
    }

    @Override
    protected void onDeactivated() {
    }

    @Override
    public void tick() {
        super.tick();
        if (getLevel().isClientSide()) return;
        if (getLevel().getGameTime() % 10 != 0) return;
        if (!itemQueue.isEmpty()) return;

        if (pullMode == PULL_AUTOMATIC) {
            runPullStepAndExport();
        } else if (pullMode == PULL_SINGLE_SWEEP && pendingSweeps > 0) {
            runSweepStep();
        }
    }

    private void runSweepStep() {
        boolean moved = runPullStepAndExport();
        if (!moved) return;

        if (isFilterEmpty() || mode >= MODE_RANDOM_ALLSTACK) {
            pendingSweeps = Math.max(0, pendingSweeps - 1);
            setChanged();
        }
    }

    private boolean runPullStepAndExport() {
        if (!canConductorWork()) return false;

        SourceAccessor source = getRearSource();
        if (source == null) return false;

        boolean moved = runPullStep(source);
        if (moved) {
            active = true;
            pushBlockState();
            exportQueue();
            scheduleTick(4);
            setChanged();
        }
        return moved;
    }

    private boolean runPullStep(SourceAccessor source) {
        if (isFilterEmpty()) {
            ItemStack stack = collectFirstWholeStack(source);
            if (stack.isEmpty()) return false;
            enqueue(
                    stack,
                    SortingMachineRules.usesDefaultRoute(mode)
                            ? defaultColour
                            : FilterRules.NO_COLOUR);
            return true;
        }

        return switch (mode) {
            case MODE_ANYSTACK_SEQUENTIAL -> runAnyStackSequential(source);
            case MODE_ALLSTACK_SEQUENTIAL -> runAllStackSequential(source);
            case MODE_RANDOM_ALLSTACK -> runRandomAllStack(source);
            case MODE_ANY_ITEM,
                    MODE_ANY_ITEM_DEFAULT,
                    MODE_ANY_ITEM_WHOLE_STACK,
                    MODE_WHOLE_STACK_DEFAULT -> runAnyItem(source, mode);
            default -> false;
        };
    }

    private boolean runAnyStackSequential(SourceAccessor source) {
        ensureCurrentColumn();
        int match = findAnyMatch(source, currentColumn);
        if (match < 0) return false;

        ItemStack template = filterInventory.getItem(match);
        ItemStack extracted = collectExact(source, template, template.getCount());
        if (extracted.isEmpty()) return false;

        enqueue(extracted, columnColours[currentColumn]);
        advanceColumn();
        return true;
    }

    private boolean runAllStackSequential(SourceAccessor source) {
        ensureCurrentColumn();
        if (!columnSatisfied(source, currentColumn)) return false;

        extractEntireColumn(source, currentColumn);
        advanceColumn();
        return true;
    }

    private boolean runRandomAllStack(SourceAccessor source) {
        for (int column = 0; column < COLUMNS; column++) {
            if (!columnSatisfied(source, column)) continue;
            extractEntireColumn(source, column);
            return true;
        }
        return false;
    }

    private boolean runAnyItem(
            SourceAccessor source,
            int sortMode) {

        int match = findAnyMatch(source, -1);
        if (match >= 0) {
            ItemStack template = filterInventory.getItem(match);
            int available = availableCount(source, template);
            int requested = SortingMachineRules.requestedAmount(
                    sortMode,
                    template.getCount(),
                    available,
                    template.getMaxStackSize());

            if (requested <= 0) return false;

            ItemStack extracted = collectExact(
                    source,
                    template,
                    requested);

            if (extracted.isEmpty()) return false;
            enqueue(extracted, columnColours[match & 7]);
            return true;
        }

        if (!SortingMachineRules.usesDefaultRoute(sortMode)) {
            return false;
        }

        ItemStack unmatched = collectFirstWholeStack(source);
        if (unmatched.isEmpty()) return false;
        enqueue(unmatched, defaultColour);
        return true;
    }

    private void extractEntireColumn(SourceAccessor source, int column) {
        for (int row = 0; row < ROWS; row++) {
            ItemStack template = filterInventory.getItem(index(row, column));
            if (template.isEmpty()) continue;

            ItemStack extracted = collectExact(
                    source,
                    template,
                    template.getCount());
            if (!extracted.isEmpty()) {
                enqueue(extracted, columnColours[column]);
            }
        }
    }

    private void enqueue(ItemStack stack, int colour) {
        PneumaticTubePayload payload = new PneumaticTubePayload(stack);
        FilterRules.applyOutputColour(payload, colour);
        itemQueue.add(payload);
        drawSortingPower(stack);
    }

    private void drawSortingPower(ItemStack stack) {
        conductor.applyPower(
                -SortingMachineRules.powerCostForItems(stack.getCount()));
    }

    //region Inline pneumatic sorting

    @Override
    public boolean canAcceptPayload(
            int s,
            PneumaticTubePayload payload,
            PneumaticTransportMode transportMode) {

        if (!super.canAcceptPayload(s, payload, transportMode)) {
            return false;
        }

        // RP2 only requires power for normal sorting input. Backstuff from the
        // output side must remain acceptable even when the machine is unpowered.
        if (transportMode == PneumaticTransportMode.PASSIVE_NORMAL
                && !canConductorWork()) {
            return false;
        }

        if (transportMode != PneumaticTransportMode.PASSIVE_NORMAL
                || s != (side ^ 1)
                || isFilterEmpty()) {
            return true;
        }

        return findMatchingTemplate(payload.getItemStack()) >= 0
                || SortingMachineRules.acceptsUnmatched(mode, false);
    }

    @Override
    public boolean insertPayload(int s, PneumaticTubePayload payload) {
        boolean normalInput = canAcceptPayload(
                s,
                payload,
                PneumaticTransportMode.PASSIVE_NORMAL);

        if (normalInput) {
            int match = findMatchingTemplate(payload.getItemStack());

            if (match >= 0) {
                FilterRules.applyOutputColour(
                        payload,
                        columnColours[match & 7]);
            } else if (isFilterEmpty()) {
                FilterRules.applyOutputColour(
                        payload,
                        FilterRules.NO_COLOUR);
            } else {
                FilterRules.applyOutputColour(payload, defaultColour);
            }
        }

        boolean inserted = super.insertPayload(s, payload);
        if (inserted && normalInput) {
            drawSortingPower(payload.getItemStack());
        }
        return inserted;
    }

    //endregion

    //region Source inventory helpers

    private SourceAccessor getRearSource() {
        CenterLookup lookup = CenterLookup.lookupStraightCenter(
                getLevel(),
                getBlockPos(),
                side ^ 1);

        if (lookup.tile == null) return null;
        Direction extractDirection = Direction.values()[lookup.otherDirection];

        if (lookup.tile instanceof WorldlyContainer container) {
            return new WorldlySource(
                    container,
                    extractDirection,
                    container.getSlotsForFace(extractDirection));
        }

        var level = Objects.requireNonNull(lookup.tile.getLevel());
        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK,
                lookup.tile.getBlockPos(),
                extractDirection);

        return handler == null ? null : new HandlerSource(handler);
    }

    private int findAnyMatch(SourceAccessor source, int column) {
        int[] accumulated = new int[FILTER_SIZE];

        for (int sourceSlot = 0; sourceSlot < source.size(); sourceSlot++) {
            ItemStack stack = source.getStack(sourceSlot);
            if (stack.isEmpty()) continue;

            int extractable = source.simulateExtract(
                    sourceSlot,
                    stack.getCount());
            if (extractable <= 0) continue;

            for (int filterSlot = 0; filterSlot < FILTER_SIZE; filterSlot++) {
                if (column >= 0 && (filterSlot & 7) != column) continue;

                ItemStack template = filterInventory.getItem(filterSlot);
                if (!FilterRules.matches(template, stack)) continue;

                accumulated[filterSlot] += extractable;
                if (accumulated[filterSlot] >= template.getCount()) {
                    return filterSlot;
                }
            }
        }

        return -1;
    }

    private boolean columnSatisfied(SourceAccessor source, int column) {
        if (!columnHasTemplates(column)) return false;

        List<Requirement> requirements = new ArrayList<>();

        for (int row = 0; row < ROWS; row++) {
            ItemStack template = filterInventory.getItem(index(row, column));
            if (template.isEmpty()) continue;

            Requirement found = null;
            for (Requirement requirement : requirements) {
                if (FilterRules.matches(requirement.template, template)) {
                    found = requirement;
                    break;
                }
            }

            if (found == null) {
                requirements.add(new Requirement(
                        template.copy(),
                        template.getCount()));
            } else {
                found.count += template.getCount();
            }
        }

        for (Requirement requirement : requirements) {
            if (availableCount(source, requirement.template)
                    < requirement.count) {
                return false;
            }
        }
        return true;
    }

    private int availableCount(SourceAccessor source, ItemStack template) {
        int count = 0;
        for (int slot = 0; slot < source.size(); slot++) {
            ItemStack stack = source.getStack(slot);
            if (!FilterRules.matches(template, stack)) continue;
            count += source.simulateExtract(slot, stack.getCount());
        }
        return count;
    }

    private ItemStack collectExact(
            SourceAccessor source,
            ItemStack template,
            int requested) {

        int available = availableCount(source, template);
        int amount = Math.min(requested, template.getMaxStackSize());

        if (available < amount || amount <= 0) return ItemStack.EMPTY;

        ItemStack result = ItemStack.EMPTY;
        int remaining = amount;

        for (int slot = 0; slot < source.size() && remaining > 0; slot++) {
            ItemStack stack = source.getStack(slot);
            if (!FilterRules.matches(template, stack)) continue;

            ItemStack removed = source.extract(slot, remaining);
            if (removed.isEmpty()) continue;

            if (result.isEmpty()) result = removed.copy();
            else result.grow(removed.getCount());

            remaining -= removed.getCount();
        }

        return remaining == 0 ? result : ItemStack.EMPTY;
    }

    private ItemStack collectFirstWholeStack(SourceAccessor source) {
        for (int slot = 0; slot < source.size(); slot++) {
            ItemStack stack = source.getStack(slot);
            if (stack.isEmpty()) continue;

            int available = source.simulateExtract(slot, stack.getCount());
            if (available <= 0) continue;

            return source.extract(slot, available);
        }
        return ItemStack.EMPTY;
    }

    //endregion

    //region Filter / column state

    private boolean isFilterEmpty() {
        for (int i = 0; i < FILTER_SIZE; i++) {
            if (!filterInventory.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    private int findMatchingTemplate(ItemStack stack) {
        for (int i = 0; i < FILTER_SIZE; i++) {
            if (FilterRules.matches(filterInventory.getItem(i), stack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean columnHasTemplates(int column) {
        for (int row = 0; row < ROWS; row++) {
            if (!filterInventory.getItem(index(row, column)).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void ensureCurrentColumn() {
        if (columnHasTemplates(currentColumn)) return;

        for (int i = 0; i < COLUMNS; i++) {
            currentColumn = (currentColumn + 1) % COLUMNS;
            if (columnHasTemplates(currentColumn)) return;
        }
        currentColumn = 0;
    }

    private void advanceColumn() {
        int start = currentColumn;

        for (int i = 0; i < COLUMNS; i++) {
            currentColumn = (currentColumn + 1) % COLUMNS;

            if (pullMode == PULL_SINGLE_SWEEP
                    && currentColumn <= start
                    && pendingSweeps > 0) {
                pendingSweeps--;
            }

            if (columnHasTemplates(currentColumn)) {
                setChanged();
                return;
            }
        }

        currentColumn = 0;
        setChanged();
    }

    public static int index(int row, int column) {
        return row * COLUMNS + column;
    }

    //endregion

    //region GUI

    @Override
    public ItemInteractionResult useItemOn(
            ItemStack held,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {

        ItemInteractionResult parent = super.useItemOn(
                held, player, hand, hit);
        if (parent.consumesAction()) return parent;

        openMenu(player);
        return ItemInteractionResult.sidedSuccess(getLevel().isClientSide());
    }

    @Override
    public InteractionResult useWithoutItem(
            Player player,
            BlockHitResult hit) {

        openMenu(player);
        return InteractionResult.sidedSuccess(getLevel().isClientSide());
    }

    private void openMenu(Player player) {
        if (getLevel().isClientSide()) return;

        CCLMenuType.openMenu(
                (ServerPlayer) player,
                new SimpleMenuProvider(
                        (id, inventory, p) ->
                                new SortingMachineMenu(inventory, this, id),
                        getBlockState().getBlock().getName()),
                packet -> packet.writePos(getBlockPos()));
    }

    public BaseContainer getFilterInventory() {
        return filterInventory;
    }

    public int getColumnColour(int column) {
        return columnColours[column];
    }

    public void cycleColumnColour(int column, boolean forward) {
        columnColours[column] = cycleColour(
                columnColours[column],
                forward);
        setChanged();
    }

    public int getMode() {
        return mode;
    }

    public void cycleMode(boolean forward) {
        mode = cycle(mode, 0, 6, forward);
        setChanged();
    }

    public int getPullMode() {
        return pullMode;
    }

    public void cyclePullMode(boolean forward) {
        pullMode = cycle(pullMode, 0, 2, forward);
        pendingSweeps = 0;
        setChanged();
    }

    public int getDefaultColour() {
        return defaultColour;
    }

    public void cycleDefaultColour(boolean forward) {
        defaultColour = cycleColour(defaultColour, forward);
        setChanged();
    }

    public int getCurrentColumn() {
        return currentColumn;
    }

    //endregion

    private static int cycleColour(int colour, boolean forward) {
        return cycle(
                colour,
                FilterRules.NO_COLOUR,
                15,
                forward);
    }

    private static int cycle(
            int value,
            int min,
            int max,
            boolean forward) {

        int next = value + (forward ? 1 : -1);
        if (next > max) next = min;
        if (next < min) next = max;
        return next;
    }

    private static int sanitizeColour(int colour) {
        return colour >= FilterRules.NO_COLOUR && colour <= 15
                ? colour
                : FilterRules.NO_COLOUR;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class Requirement {
        private final ItemStack template;
        private int count;

        private Requirement(ItemStack template, int count) {
            this.template = template;
            this.count = count;
        }
    }

    private interface SourceAccessor {
        int size();
        ItemStack getStack(int slot);
        int simulateExtract(int slot, int amount);
        ItemStack extract(int slot, int amount);
    }

    private record WorldlySource(
            WorldlyContainer container,
            Direction direction,
            int[] slots) implements SourceAccessor {

        @Override
        public int size() {
            return slots.length;
        }

        @Override
        public ItemStack getStack(int slot) {
            return container.getItem(slots[slot]);
        }

        @Override
        public int simulateExtract(int slot, int amount) {
            ItemStack stack = getStack(slot);
            if (stack.isEmpty()
                    || !container.canTakeItemThroughFace(
                            slots[slot],
                            stack,
                            direction)) {
                return 0;
            }
            return Math.min(amount, stack.getCount());
        }

        @Override
        public ItemStack extract(int slot, int amount) {
            ItemStack stack = getStack(slot);
            if (stack.isEmpty()
                    || !container.canTakeItemThroughFace(
                            slots[slot],
                            stack,
                            direction)) {
                return ItemStack.EMPTY;
            }
            return container.removeItem(
                    slots[slot],
                    Math.min(amount, stack.getCount()));
        }
    }

    private record HandlerSource(
            IItemHandler handler) implements SourceAccessor {

        @Override
        public int size() {
            return handler.getSlots();
        }

        @Override
        public ItemStack getStack(int slot) {
            return handler.getStackInSlot(slot);
        }

        @Override
        public int simulateExtract(int slot, int amount) {
            return handler.extractItem(slot, amount, true).getCount();
        }

        @Override
        public ItemStack extract(int slot, int amount) {
            return handler.extractItem(slot, amount, false);
        }
    }
}
