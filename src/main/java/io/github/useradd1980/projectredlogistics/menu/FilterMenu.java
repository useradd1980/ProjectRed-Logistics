package io.github.useradd1980.projectredlogistics.menu;

import codechicken.lib.inventory.container.CCLMenuType;
import io.github.useradd1980.projectredlogistics.block.entity.FilterBlockEntity;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import mrtjp.projectred.lib.InventoryLib;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class FilterMenu extends AbstractContainerMenu {

    public static final CCLMenuType<FilterMenu> FACTORY =
            (windowId, playerInventory, packet) -> {
                BlockEntity blockEntity = playerInventory.player.level()
                        .getBlockEntity(Objects.requireNonNull(packet).readPos());
                if (!(blockEntity instanceof FilterBlockEntity filter)) {
                    return null;
                }
                return new FilterMenu(playerInventory, filter, windowId);
            };

    private final FilterBlockEntity filter;

    public FilterMenu(
            Inventory playerInventory,
            FilterBlockEntity filter,
            int windowId) {

        super(LogisticsContent.FILTER_MENU.get(), windowId);
        this.filter = filter;

        // 3x3 RP2 Filter inventory. These are real stacks; the count in each
        // slot determines the requested extraction batch size.
        InventoryLib.addInventory(
                filter.getFilterInventory(),
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
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(filter, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        // Filter slots are 0..8; player inventory/hotbar are 9..44.
        if (slotIndex < FilterBlockEntity.FILTER_SIZE) {
            if (!moveItemStackTo(
                    stack,
                    FilterBlockEntity.FILTER_SIZE,
                    slots.size(),
                    true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(
                    stack,
                    0,
                    FilterBlockEntity.FILTER_SIZE,
                    false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    public FilterBlockEntity getFilter() {
        return filter;
    }
}
