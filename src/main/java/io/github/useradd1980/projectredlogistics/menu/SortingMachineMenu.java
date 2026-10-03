package io.github.useradd1980.projectredlogistics.menu;

import codechicken.lib.inventory.container.CCLMenuType;
import io.github.useradd1980.projectredlogistics.block.entity.SortingMachineBlockEntity;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import io.github.useradd1980.projectredlogistics.sorting.SortingMachineRules;
import mrtjp.projectred.core.inventory.container.SimpleDataSlot;
import mrtjp.projectred.lib.InventoryLib;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Arrays;
import java.util.Objects;

public class SortingMachineMenu extends AbstractContainerMenu {

    public static final CCLMenuType<SortingMachineMenu> FACTORY =
            (windowId, playerInventory, packet) -> {
                BlockEntity blockEntity = playerInventory.player.level()
                        .getBlockEntity(
                                Objects.requireNonNull(packet).readPos());
                if (!(blockEntity instanceof SortingMachineBlockEntity sorter)) {
                    return null;
                }
                return new SortingMachineMenu(
                        playerInventory,
                        sorter,
                        windowId);
            };

    public static final int BUTTON_MODE_NEXT = 0;
    public static final int BUTTON_MODE_PREVIOUS = 1;
    public static final int BUTTON_PULL_NEXT = 2;
    public static final int BUTTON_PULL_PREVIOUS = 3;
    public static final int BUTTON_DEFAULT_NEXT = 4;
    public static final int BUTTON_DEFAULT_PREVIOUS = 5;
    public static final int BUTTON_COLUMN_BASE = 16;

    private final SortingMachineBlockEntity sorter;

    private final int[] columnColours =
            new int[SortingMachineBlockEntity.COLUMNS];
    private int mode;
    private int pullMode;
    private int defaultColour;
    private int currentColumn;
    private int condCharge;
    private int condFlow;

    public SortingMachineMenu(
            Inventory playerInventory,
            SortingMachineBlockEntity sorter,
            int windowId) {

        super(LogisticsContent.SORTING_MACHINE_MENU.get(), windowId);
        this.sorter = sorter;
        Arrays.fill(columnColours, -1);

        InventoryLib.addInventory(
                sorter.getFilterInventory(),
                0,
                26,
                18,
                SortingMachineBlockEntity.COLUMNS,
                SortingMachineBlockEntity.ROWS,
                this::addSlot);

        InventoryLib.addPlayerInventory(
                playerInventory,
                8,
                140,
                this::addSlot);

        for (int i = 0; i < SortingMachineBlockEntity.COLUMNS; i++) {
            final int column = i;
            addDataSlot(new SimpleDataSlot(
                    () -> sorter.getColumnColour(column),
                    value -> columnColours[column] = value));
        }

        addDataSlot(new SimpleDataSlot(
                sorter::getMode,
                value -> mode = value));
        addDataSlot(new SimpleDataSlot(
                sorter::getPullMode,
                value -> pullMode = value));
        addDataSlot(new SimpleDataSlot(
                sorter::getDefaultColour,
                value -> defaultColour = value));
        addDataSlot(new SimpleDataSlot(
                sorter::getCurrentColumn,
                value -> currentColumn = value));

        addDataSlot(new SimpleDataSlot(
                sorter::getConductorCharge,
                value -> condCharge = value));
        addDataSlot(new SimpleDataSlot(
                () -> sorter.getConductorFlow() & 0xFFFF,
                value -> condFlow =
                        condFlow & 0xFFFF0000 | value & 0xFFFF));
        addDataSlot(new SimpleDataSlot(
                () -> sorter.getConductorFlow() >> 16 & 0xFFFF,
                value -> condFlow =
                        condFlow & 0xFFFF | value << 16));
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(sorter, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case BUTTON_MODE_NEXT -> sorter.cycleMode(true);
            case BUTTON_MODE_PREVIOUS -> sorter.cycleMode(false);
            case BUTTON_PULL_NEXT -> sorter.cyclePullMode(true);
            case BUTTON_PULL_PREVIOUS -> sorter.cyclePullMode(false);
            case BUTTON_DEFAULT_NEXT -> sorter.cycleDefaultColour(true);
            case BUTTON_DEFAULT_PREVIOUS -> sorter.cycleDefaultColour(false);
            default -> {
                if (buttonId < BUTTON_COLUMN_BASE
                        || buttonId >= BUTTON_COLUMN_BASE
                        + SortingMachineBlockEntity.COLUMNS * 2) {
                    return false;
                }

                int relative = buttonId - BUTTON_COLUMN_BASE;
                int column = relative / 2;
                boolean forward = relative % 2 == 0;
                sorter.cycleColumnColour(column, forward);
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < SortingMachineBlockEntity.FILTER_SIZE) {
            if (!moveItemStackTo(
                    stack,
                    SortingMachineBlockEntity.FILTER_SIZE,
                    slots.size(),
                    true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(
                    stack,
                    0,
                    SortingMachineBlockEntity.FILTER_SIZE,
                    false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    public int getColumnColour(int column) {
        return columnColours[column];
    }

    public int getMode() {
        return mode;
    }

    public int getPullMode() {
        return pullMode;
    }

    public int getDefaultColour() {
        return defaultColour;
    }

    public int getCurrentColumn() {
        return currentColumn;
    }

    public int getConductorCharge() {
        return condCharge;
    }

    public int getChargeScaled(int scale) {
        return Math.min(scale, scale * condCharge / 1000);
    }

    public int getFlowScaled(int scale) {
        return scale * Integer.bitCount(condFlow) / 32;
    }

    public boolean canConductorWork() {
        return SortingMachineRules.hasOperatingPower(condCharge);
    }
}
