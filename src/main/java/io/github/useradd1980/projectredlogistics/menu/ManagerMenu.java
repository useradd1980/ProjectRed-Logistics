package io.github.useradd1980.projectredlogistics.menu;

import codechicken.lib.inventory.container.CCLMenuType;
import io.github.useradd1980.projectredlogistics.block.entity.ManagerBlockEntity;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import mrtjp.projectred.core.inventory.container.SimpleDataSlot;
import mrtjp.projectred.lib.InventoryLib;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class ManagerMenu extends AbstractContainerMenu {

    public static final CCLMenuType<ManagerMenu> FACTORY =
            (windowId, playerInventory, packet) -> {
                BlockEntity blockEntity = playerInventory.player.level()
                        .getBlockEntity(
                                Objects.requireNonNull(packet).readPos());

                if (!(blockEntity instanceof ManagerBlockEntity manager)) {
                    return null;
                }

                return new ManagerMenu(
                        playerInventory,
                        manager,
                        windowId);
            };

    public static final int BUTTON_MODE_NEXT = 0;
    public static final int BUTTON_MODE_PREVIOUS = 1;
    public static final int BUTTON_PRIORITY_NEXT = 2;
    public static final int BUTTON_PRIORITY_PREVIOUS = 3;
    public static final int BUTTON_COLOUR_NEXT = 4;
    public static final int BUTTON_COLOUR_PREVIOUS = 5;

    private final ManagerBlockEntity manager;

    private int mode;
    private int routeColour = -1;
    private int priority;
    private int condCharge;
    private int condFlow;

    public ManagerMenu(
            Inventory playerInventory,
            ManagerBlockEntity manager,
            int windowId) {

        super(LogisticsContent.MANAGER_MENU.get(), windowId);
        this.manager = manager;

        InventoryLib.addInventory(
                manager.getTemplateInventory(),
                0,
                44,
                18,
                6,
                4,
                this::addSlot);

        InventoryLib.addPlayerInventory(
                playerInventory,
                8,
                104,
                this::addSlot);

        addDataSlot(new SimpleDataSlot(
                manager::getMode,
                value -> mode = value));
        addDataSlot(new SimpleDataSlot(
                manager::getRouteColour,
                value -> routeColour = value));
        addDataSlot(new SimpleDataSlot(
                manager::getPriority,
                value -> priority = value));

        addDataSlot(new SimpleDataSlot(
                manager::getConductorCharge,
                value -> condCharge = value));
        addDataSlot(new SimpleDataSlot(
                () -> manager.getConductorFlow() & 0xFFFF,
                value -> condFlow =
                        condFlow & 0xFFFF0000 | value & 0xFFFF));
        addDataSlot(new SimpleDataSlot(
                () -> manager.getConductorFlow() >> 16 & 0xFFFF,
                value -> condFlow =
                        condFlow & 0xFFFF | value << 16));
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(manager, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case BUTTON_MODE_NEXT -> manager.cycleMode(true);
            case BUTTON_MODE_PREVIOUS -> manager.cycleMode(false);
            case BUTTON_PRIORITY_NEXT -> manager.cyclePriority(true);
            case BUTTON_PRIORITY_PREVIOUS -> manager.cyclePriority(false);
            case BUTTON_COLOUR_NEXT -> manager.cycleRouteColour(true);
            case BUTTON_COLOUR_PREVIOUS -> manager.cycleRouteColour(false);
            default -> {
                return false;
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

        if (slotIndex < ManagerBlockEntity.TEMPLATE_SIZE) {
            if (!moveItemStackTo(
                    stack,
                    ManagerBlockEntity.TEMPLATE_SIZE,
                    slots.size(),
                    true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(
                    stack,
                    0,
                    ManagerBlockEntity.TEMPLATE_SIZE,
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

    public int getMode() {
        return mode;
    }

    public int getRouteColour() {
        return routeColour;
    }

    public int getPriority() {
        return priority;
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
        return condCharge > 600;
    }

    public boolean isFlowFull() {
        return condFlow == -1;
    }
}
