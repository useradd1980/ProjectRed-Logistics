package io.github.useradd1980.projectredlogistics.block.entity;

import codechicken.lib.inventory.container.CCLMenuType;
import codechicken.lib.vec.Vector3;
import io.github.useradd1980.projectredlogistics.filter.FilterRules;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import io.github.useradd1980.projectredlogistics.menu.ManagerMenu;
import io.github.useradd1980.projectredlogistics.power.PoweredPneumaticDeviceBlockEntity;
import io.github.useradd1980.projectredlogistics.routing.LogisticsRoutingData;
import io.github.useradd1980.projectredlogistics.routing.ManagerRoutingData;
import mrtjp.projectred.core.CenterLookup;
import mrtjp.projectred.core.inventory.BaseContainer;
import mrtjp.projectred.expansion.graphs.GraphContainer;
import mrtjp.projectred.expansion.graphs.GraphRoute;
import mrtjp.projectred.expansion.part.GraphContainerTubePart;
import mrtjp.projectred.expansion.part.PneumaticTubePayload;
import mrtjp.projectred.expansion.pneumatics.PneumaticTransportMode;
import mrtjp.projectred.lib.InventoryLib;
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.OptionalInt;
import java.util.Set;

/**
 * RedPower 2 pr6-style Manager.
 *
 * The 24-slot internal inventory is the stock template. In Stock mode the
 * attached rear inventory is kept at the configured quantities: surplus is
 * exported and shortages are requested from lower-priority Managers on the
 * same pneumatic network. In Excess mode, only items not represented in the
 * template are exported.
 */
public class ManagerBlockEntity
        extends PoweredPneumaticDeviceBlockEntity {

    public static final int TEMPLATE_SIZE = 24;

    public static final int MODE_STOCK = 0;
    public static final int MODE_EXCESS = 1;

    private static final String TAG_TEMPLATE = "template";
    private static final String TAG_MODE = "mode";
    private static final String TAG_COLOUR = "route_colour";
    private static final String TAG_PRIORITY = "priority";
    private static final String TAG_REQUEST_CURSOR = "request_cursor";

    private final BaseContainer templateInventory =
            new BaseContainer(TEMPLATE_SIZE);

    private int mode = MODE_STOCK;
    private int routeColour = FilterRules.NO_COLOUR;
    private int priority = 0;
    private int requestCursor = 0;
    private int requestCooldown = 0;

    public ManagerBlockEntity(BlockPos pos, BlockState state) {
        super(LogisticsContent.MANAGER_BLOCK_ENTITY.get(), pos, state);
        templateInventory.addListener(container -> setChanged());
    }

    //region persistence
    @Override
    public void saveToNBT(
            CompoundTag tag,
            HolderLookup.Provider lookupProvider) {

        super.saveToNBT(tag, lookupProvider);
        templateInventory.saveTo(tag, TAG_TEMPLATE, lookupProvider);
        tag.putInt(TAG_MODE, mode);
        tag.putInt(TAG_COLOUR, routeColour);
        tag.putInt(TAG_PRIORITY, priority);
        tag.putInt(TAG_REQUEST_CURSOR, requestCursor);
    }

    @Override
    public void loadFromNBT(
            CompoundTag tag,
            HolderLookup.Provider lookupProvider) {

        super.loadFromNBT(tag, lookupProvider);
        templateInventory.loadFrom(tag, TAG_TEMPLATE, lookupProvider);
        mode = clamp(tag.getInt(TAG_MODE), MODE_STOCK, MODE_EXCESS);
        routeColour = sanitizeColour(tag.contains(TAG_COLOUR)
                ? tag.getInt(TAG_COLOUR)
                : FilterRules.NO_COLOUR);
        priority = clamp(tag.getInt(TAG_PRIORITY), 0, 9);
        requestCursor = clamp(
                tag.getInt(TAG_REQUEST_CURSOR),
                0,
                TEMPLATE_SIZE - 1);
    }
    //endregion

    @Override
    public void onBlockRemoved() {
        super.onBlockRemoved();
        dropInventory(
                templateInventory,
                getLevel(),
                Vector3.fromBlockPos(getBlockPos()));
    }

    @Override
    protected void onActivated() {
        // The Manager is autonomous; redstone is not required.
    }

    @Override
    protected void onDeactivated() {
    }

    @Override
    public void tick() {
        super.tick();
        if (getLevel().isClientSide()) return;

        if (requestCooldown > 0) {
            requestCooldown--;
        }

        if (getLevel().getGameTime() % 10 != 0) return;
        if (!canConductorWork()) return;
        if (!itemQueue.isEmpty()) return;

        RearInventory rear = getRearInventory();
        if (rear == null) return;

        if (exportOneSurplus(rear)) return;

        if (mode == MODE_STOCK && requestCooldown == 0) {
            requestOneMissingStack(rear);
        }
    }

    //region pneumatic endpoint
    @Override
    public boolean canConnectTube(int s) {
        return s == side;
    }

    @Override
    public boolean canAcceptPayload(
            int s,
            PneumaticTubePayload payload,
            PneumaticTransportMode transportMode) {

        if (s != side || !canConductorWork()) {
            return false;
        }

        var target = ManagerRoutingData.getTarget(payload);
        if (target.isPresent() && !target.get().equals(getBlockPos())) {
            return false;
        }

        int payloadPriority =
                ManagerRoutingData.getPriority(payload).orElse(0);
        if (payloadPriority > priority) {
            return false;
        }

        if (!coloursCompatible(payload)) {
            return false;
        }

        RearInventory rear = getRearInventory();
        if (rear == null) return false;

        ItemStack stack = payload.getItemStack();
        int wanted = acceptedCount(rear, stack);
        if (wanted <= 0) return false;

        ItemStack probe = stack.copy();
        probe.setCount(Math.min(wanted, stack.getCount()));
        return rear.insert(probe, true) > 0;
    }

    @Override
    public boolean insertPayload(int s, PneumaticTubePayload payload) {
        if (!canAcceptPayload(
                s,
                payload,
                PneumaticTransportMode.PASSIVE_NORMAL)
                && !canAcceptPayload(
                s,
                payload,
                PneumaticTransportMode.PASSIVE_BACKSTUFF)) {
            return false;
        }

        RearInventory rear = getRearInventory();
        if (rear == null) return false;

        ItemStack travelling = payload.getItemStack();
        int wanted = Math.min(
                travelling.getCount(),
                acceptedCount(rear, travelling));
        if (wanted <= 0) return false;

        ItemStack toInsert = travelling.copy();
        toInsert.setCount(wanted);

        int inserted = rear.insert(toInsert, false);
        if (inserted <= 0) return false;

        travelling.shrink(inserted);
        drawManagerPower(inserted);

        active = true;
        pushBlockState();
        scheduleTick(5);
        setChanged();

        return travelling.isEmpty();
    }
    //endregion

    //region stock management
    private boolean exportOneSurplus(RearInventory rear) {
        for (int slot = 0; slot < rear.size(); slot++) {
            ItemStack stack = rear.getStack(slot);
            if (stack.isEmpty()) continue;

            int amount;

            if (mode == MODE_EXCESS) {
                if (hasTemplate(stack)) continue;
                amount = stack.getCount();
            } else {
                int target = targetCount(stack);
                int present = countInRear(rear, stack);
                int excess = present - target;
                if (excess <= 0) continue;
                amount = Math.min(excess, stack.getCount());
            }

            ItemStack extracted = rear.extract(slot, amount);
            if (extracted.isEmpty()) continue;

            enqueueOutput(extracted);
            return true;
        }

        return false;
    }

    private void requestOneMissingStack(RearInventory rear) {
        for (int offset = 0; offset < TEMPLATE_SIZE; offset++) {
            int slot = (requestCursor + offset) % TEMPLATE_SIZE;
            ItemStack template = templateInventory.getItem(slot);
            if (template.isEmpty()) continue;

            int target = targetCount(template);
            int present = countInRear(rear, template);
            int missing = target - present;
            if (missing <= 0) continue;

            requestCursor = (slot + 1) % TEMPLATE_SIZE;

            if (requestFromNetwork(
                    template,
                    Math.min(64, missing))) {
                requestCooldown = 20;
                setChanged();
            }
            return;
        }
    }

    private void enqueueOutput(ItemStack stack) {
        PneumaticTubePayload payload =
                new PneumaticTubePayload(stack);
        FilterRules.applyOutputColour(payload, routeColour);

        itemQueue.add(payload);
        drawManagerPower(stack.getCount());

        active = true;
        pushBlockState();
        exportQueue();
        scheduleTick(5);
        setChanged();
    }

    private int acceptedCount(RearInventory rear, ItemStack stack) {
        if (!hasTemplate(stack)) {
            return 0;
        }

        if (mode == MODE_EXCESS) {
            return stack.getCount();
        }

        int missing = targetCount(stack) - countInRear(rear, stack);
        return Math.max(0, missing);
    }

    private int targetCount(ItemStack stack) {
        int count = 0;
        for (int slot = 0; slot < TEMPLATE_SIZE; slot++) {
            ItemStack template = templateInventory.getItem(slot);
            if (FilterRules.matches(template, stack)) {
                count += template.getCount();
            }
        }
        return count;
    }

    private boolean hasTemplate(ItemStack stack) {
        for (int slot = 0; slot < TEMPLATE_SIZE; slot++) {
            if (FilterRules.matches(
                    templateInventory.getItem(slot),
                    stack)) {
                return true;
            }
        }
        return false;
    }

    private int countInRear(
            RearInventory rear,
            ItemStack template) {

        int count = 0;
        for (int slot = 0; slot < rear.size(); slot++) {
            ItemStack stack = rear.getStack(slot);
            if (FilterRules.matches(template, stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }
    //endregion

    //region Manager-to-Manager requests
    private boolean requestFromNetwork(
            ItemStack template,
            int amount) {

        GraphContainerTubePart start = getFrontTube();
        if (start == null) return false;

        Set<BlockPos> visitedManagers = new HashSet<>();

        if (requestFromManagersAtTube(
                start,
                template,
                amount,
                visitedManagers)) {
            return true;
        }

        Iterator<GraphRoute> routes =
                start.getNode().getRouteTable().routeIterator();

        while (routes.hasNext()) {
            GraphContainer destination =
                    routes.next().end().container;

            if (!(destination instanceof GraphContainerTubePart tube)) {
                continue;
            }

            if (requestFromManagersAtTube(
                    tube,
                    template,
                    amount,
                    visitedManagers)) {
                return true;
            }
        }

        return false;
    }

    private boolean requestFromManagersAtTube(
            GraphContainerTubePart tube,
            ItemStack template,
            int amount,
            Set<BlockPos> visitedManagers) {

        for (int s = 0; s < 6; s++) {
            CenterLookup lookup = CenterLookup.lookupStraightCenter(
                    getLevel(),
                    tube.pos(),
                    s);

            if (!(lookup.tile instanceof ManagerBlockEntity manager)
                    || manager == this
                    || !manager.canConnectTube(lookup.otherDirection)
                    || !visitedManagers.add(manager.getBlockPos())) {
                continue;
            }

            if (manager.supplyRequest(
                    template,
                    amount,
                    priority,
                    routeColour,
                    getBlockPos())) {
                return true;
            }
        }

        return false;
    }

    private boolean supplyRequest(
            ItemStack template,
            int amount,
            int requesterPriority,
            int requesterColour,
            BlockPos target) {

        if (!canConductorWork()
                || !itemQueue.isEmpty()
                || requesterPriority <= priority
                || !hasTemplate(template)) {
            return false;
        }

        if (routeColour != FilterRules.NO_COLOUR
                && requesterColour != routeColour) {
            return false;
        }

        RearInventory rear = getRearInventory();
        if (rear == null) return false;

        ItemStack extracted = extractMatching(
                rear,
                template,
                amount);
        if (extracted.isEmpty()) return false;

        PneumaticTubePayload payload =
                new PneumaticTubePayload(extracted);
        FilterRules.applyOutputColour(payload, routeColour);
        ManagerRoutingData.setRequest(
                payload,
                target,
                requesterPriority);

        itemQueue.add(payload);
        drawManagerPower(extracted.getCount());

        active = true;
        pushBlockState();
        exportQueue();
        scheduleTick(5);
        setChanged();
        return true;
    }

    private GraphContainerTubePart getFrontTube() {
        CenterLookup lookup = CenterLookup.lookupStraightCenter(
                getLevel(),
                getBlockPos(),
                side);

        return lookup.part instanceof GraphContainerTubePart tube
                ? tube
                : null;
    }
    //endregion

    //region rear inventory access
    private RearInventory getRearInventory() {
        CenterLookup lookup = CenterLookup.lookupStraightCenter(
                getLevel(),
                getBlockPos(),
                side ^ 1);

        if (lookup.tile == null) return null;

        int inventorySide = lookup.otherDirection;

        if (lookup.tile instanceof WorldlyContainer container) {
            return new WorldlyRearInventory(
                    container,
                    Direction.values()[inventorySide]);
        }

        IItemHandler handler = getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK,
                lookup.tile.getBlockPos(),
                Direction.values()[inventorySide]);

        return handler == null
                ? null
                : new HandlerRearInventory(handler);
    }

    private ItemStack extractMatching(
            RearInventory rear,
            ItemStack template,
            int amount) {

        ItemStack result = ItemStack.EMPTY;
        int remaining = amount;

        for (int slot = 0;
                slot < rear.size() && remaining > 0;
                slot++) {

            ItemStack stack = rear.getStack(slot);
            if (!FilterRules.matches(template, stack)) continue;

            ItemStack removed = rear.extract(slot, remaining);
            if (removed.isEmpty()) continue;

            if (result.isEmpty()) {
                result = removed.copy();
            } else {
                result.grow(removed.getCount());
            }

            remaining -= removed.getCount();
        }

        return result;
    }

    private interface RearInventory {
        int size();
        ItemStack getStack(int slot);
        ItemStack extract(int slot, int amount);
        int insert(ItemStack stack, boolean simulate);
    }

    private record WorldlyRearInventory(
            WorldlyContainer container,
            Direction direction) implements RearInventory {

        @Override
        public int size() {
            return container.getSlotsForFace(direction).length;
        }

        @Override
        public ItemStack getStack(int slot) {
            int[] slots = container.getSlotsForFace(direction);
            return container.getItem(slots[slot]);
        }

        @Override
        public ItemStack extract(int slot, int amount) {
            int[] slots = container.getSlotsForFace(direction);
            int actualSlot = slots[slot];
            ItemStack stack = container.getItem(actualSlot);

            if (stack.isEmpty()
                    || !container.canTakeItemThroughFace(
                            actualSlot,
                            stack,
                            direction)) {
                return ItemStack.EMPTY;
            }

            return container.removeItem(
                    actualSlot,
                    Math.min(amount, stack.getCount()));
        }

        @Override
        public int insert(ItemStack stack, boolean simulate) {
            int before = stack.getCount();
            InventoryLib.injectWorldly(
                    container,
                    stack,
                    direction.ordinal(),
                    simulate);
            return before - stack.getCount();
        }
    }

    private record HandlerRearInventory(
            IItemHandler handler) implements RearInventory {

        @Override
        public int size() {
            return handler.getSlots();
        }

        @Override
        public ItemStack getStack(int slot) {
            return handler.getStackInSlot(slot);
        }

        @Override
        public ItemStack extract(int slot, int amount) {
            return handler.extractItem(slot, amount, false);
        }

        @Override
        public int insert(ItemStack stack, boolean simulate) {
            int before = stack.getCount();
            InventoryLib.injectItemHandler(handler, stack, simulate);
            return before - stack.getCount();
        }
    }
    //endregion

    private boolean coloursCompatible(PneumaticTubePayload payload) {
        OptionalInt payloadColour =
                LogisticsRoutingData.getPayloadColour(payload);

        return routeColour == FilterRules.NO_COLOUR
                || payloadColour.isEmpty()
                || payloadColour.getAsInt() == routeColour;
    }

    private void drawManagerPower(int itemCount) {
        conductor.applyPower(-25.0D * itemCount);
    }

    //region GUI/configuration
    public BaseContainer getTemplateInventory() {
        return templateInventory;
    }

    public int getMode() {
        return mode;
    }

    public int getRouteColour() {
        return routeColour;
    }

    public int getPriority() {
        return priority;
    }

    public void cycleMode(boolean forward) {
        mode = cycle(mode, MODE_STOCK, MODE_EXCESS, forward);
        setChanged();
    }

    public void cycleRouteColour(boolean forward) {
        routeColour = cycle(
                routeColour,
                FilterRules.NO_COLOUR,
                15,
                forward);
        setChanged();
    }

    public void cyclePriority(boolean forward) {
        priority = cycle(priority, 0, 9, forward);
        setChanged();
    }
    //endregion

    //region GUI opening
    @Override
    public ItemInteractionResult useItemOn(
            ItemStack held,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {

        ItemInteractionResult parent =
                super.useItemOn(held, player, hand, hit);

        if (parent.consumesAction()) {
            return parent;
        }

        openMenu(player);
        return ItemInteractionResult.sidedSuccess(
                getLevel().isClientSide());
    }

    @Override
    public InteractionResult useWithoutItem(
            Player player,
            BlockHitResult hit) {

        openMenu(player);
        return InteractionResult.sidedSuccess(
                getLevel().isClientSide());
    }

    private void openMenu(Player player) {
        if (getLevel().isClientSide()) return;

        CCLMenuType.openMenu(
                (ServerPlayer) player,
                new SimpleMenuProvider(
                        (id, inventory, p) ->
                                new ManagerMenu(inventory, this, id),
                        getBlockState().getBlock().getName()),
                packet -> packet.writePos(getBlockPos()));
    }
    //endregion

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
}
