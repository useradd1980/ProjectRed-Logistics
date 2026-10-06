package io.github.useradd1980.projectredlogistics.menu;

import codechicken.lib.inventory.container.CCLMenuType;
import io.github.useradd1980.projectredlogistics.block.entity.ItemDetectorBlockEntity;
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

public class ItemDetectorMenu extends AbstractContainerMenu {

    public static final CCLMenuType<ItemDetectorMenu> FACTORY =
            (windowId, playerInventory, packet) -> {
                BlockEntity blockEntity = playerInventory.player.level()
                        .getBlockEntity(Objects.requireNonNull(packet).readPos());

                if (!(blockEntity instanceof ItemDetectorBlockEntity detector)) {
                    return null;
                }

                return new ItemDetectorMenu(playerInventory, detector, windowId);
            };

    public static final int BUTTON_MODE_NEXT = 0;
    public static final int BUTTON_MODE_PREVIOUS = 1;

    private final ItemDetectorBlockEntity detector;
    private int mode;

    public ItemDetectorMenu(
            Inventory playerInventory,
            ItemDetectorBlockEntity detector,
            int windowId) {

        super(LogisticsContent.ITEM_DETECTOR_MENU.get(), windowId);
        this.detector = detector;

        InventoryLib.addInventory(
                detector.getFilterInventory(),
                0,
                62,
                17,
                3,
                3,
                this::addSlot);

        InventoryLib.addPlayerInventory(
                playerInventory,
                8,
                84,
                this::addSlot);

        mode = detector.getMode();
        addDataSlot(new SimpleDataSlot(
                detector::getMode,
                value -> mode = value));
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(detector, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case BUTTON_MODE_NEXT -> detector.cycleMode(true);
            case BUTTON_MODE_PREVIOUS -> detector.cycleMode(false);
            default -> {
                return false;
            }
        }

        mode = detector.getMode();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < ItemDetectorBlockEntity.FILTER_SIZE) {
            if (!moveItemStackTo(
                    stack,
                    ItemDetectorBlockEntity.FILTER_SIZE,
                    slots.size(),
                    true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(
                stack,
                0,
                ItemDetectorBlockEntity.FILTER_SIZE,
                false)) {
            return ItemStack.EMPTY;
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
}
