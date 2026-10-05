package io.github.useradd1980.projectredlogistics.menu;

import codechicken.lib.inventory.container.CCLMenuType;
import io.github.useradd1980.projectredlogistics.block.entity.BufferBlockEntity;
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

public class BufferMenu extends AbstractContainerMenu {

    public static final CCLMenuType<BufferMenu> FACTORY =
            (windowId, playerInventory, packet) -> {
                BlockEntity blockEntity = playerInventory.player.level()
                        .getBlockEntity(Objects.requireNonNull(packet).readPos());

                if (!(blockEntity instanceof BufferBlockEntity buffer)) {
                    return null;
                }

                return new BufferMenu(playerInventory, buffer, windowId);
            };

    private final BufferBlockEntity buffer;

    public BufferMenu(
            Inventory playerInventory,
            BufferBlockEntity buffer,
            int windowId) {

        super(LogisticsContent.BUFFER_MENU.get(), windowId);
        this.buffer = buffer;

        InventoryLib.addInventory(
                buffer.getInventory(),
                0,
                44,
                18,
                5,
                4,
                this::addSlot);

        InventoryLib.addPlayerInventory(
                playerInventory,
                8,
                104,
                this::addSlot);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(buffer, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < BufferBlockEntity.INVENTORY_SIZE) {
            if (!moveItemStackTo(
                    stack,
                    BufferBlockEntity.INVENTORY_SIZE,
                    slots.size(),
                    true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(
                stack,
                0,
                BufferBlockEntity.INVENTORY_SIZE,
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
}
