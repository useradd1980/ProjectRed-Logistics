package io.github.useradd1980.projectredlogistics.client.screen;

import io.github.useradd1980.projectredlogistics.menu.ItemDetectorMenu;
import mrtjp.projectred.lib.Point;
import mrtjp.projectred.redui.RedUIContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemDetectorScreen extends RedUIContainerScreen<ItemDetectorMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(
                    "projectred_logistics",
                    "textures/gui/rp2_item_detector.png");

    private static final int MODE_X = 117;
    private static final int MODE_Y = 54;
    private static final int MODE_SIZE = 14;

    public ItemDetectorScreen(
            ItemDetectorMenu menu,
            Inventory playerInventory,
            Component title) {

        super(176, 166, menu, playerInventory, title);
        titleLabelX = 60;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    public void drawBack(
            GuiGraphics graphics,
            Point mouse,
            float partialFrame) {

        int x = getFrame().x();
        int y = getFrame().y();

        graphics.blit(BACKGROUND, x, y, 0, 0, 176, 166);
        graphics.blit(
                BACKGROUND,
                x + MODE_X,
                y + MODE_Y,
                176,
                14 * menu.getMode(),
                MODE_SIZE,
                MODE_SIZE);
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int mouseButton) {

        int relativeX = (int) mouseX - getFrame().x();
        int relativeY = (int) mouseY - getFrame().y();

        if (relativeX >= MODE_X
                && relativeX < MODE_X + MODE_SIZE
                && relativeY >= MODE_Y
                && relativeY < MODE_Y + MODE_SIZE) {

            int buttonId = mouseButton == 0
                    ? ItemDetectorMenu.BUTTON_MODE_NEXT
                    : ItemDetectorMenu.BUTTON_MODE_PREVIOUS;

            return sendButton(buttonId);
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean sendButton(int buttonId) {
        if (minecraft == null
                || minecraft.player == null
                || minecraft.gameMode == null) {
            return false;
        }

        if (!menu.clickMenuButton(minecraft.player, buttonId)) {
            return false;
        }

        minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId,
                buttonId);
        return true;
    }
}
