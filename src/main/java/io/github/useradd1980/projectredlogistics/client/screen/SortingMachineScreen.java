package io.github.useradd1980.projectredlogistics.client.screen;

import io.github.useradd1980.projectredlogistics.block.entity.SortingMachineBlockEntity;
import io.github.useradd1980.projectredlogistics.menu.SortingMachineMenu;
import mrtjp.projectred.lib.Point;
import mrtjp.projectred.redui.RedUIContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SortingMachineScreen
        extends RedUIContainerScreen<SortingMachineMenu> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 222;

    private static final int[] PAINT_COLOURS = {
            0xFFFFFF, 0xFF8000, 0xFF00FF, 0x6C80FF,
            0xFFFF00, 0x00FF00, 0xFF6480, 0x535353,
            0x939393, 0x00FFFF, 0x8000FF, 0x0000FF,
            0x4F2700, 0x008000, 0xFF0000, 0x1F1F1F
    };

    private static final String[] MODE_NAMES = {
            "Anystack Sequential",
            "Allstack Sequential",
            "Random Allstack",
            "Any Item",
            "Any Item + Default",
            "Any Item Whole Stack",
            "Whole Stack + Default"
    };

    private static final String[] PULL_NAMES = {
            "Single Step",
            "Automatic",
            "Single Sweep"
    };

    public SortingMachineScreen(
            SortingMachineMenu menu,
            Inventory playerInventory,
            Component title) {

        super(WIDTH, HEIGHT, menu, playerInventory, title);
        titleLabelX = 50;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 128;
    }

    @Override
    public void drawBack(
            GuiGraphics graphics,
            Point mouse,
            float partialFrame) {

        int x = getFrame().x();
        int y = getFrame().y();

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFC6C6C6);
        graphics.fill(x + 4, y + 14, x + 172, y + 126, 0xFF6F6F6F);
        graphics.fill(x + 5, y + 15, x + 171, y + 125, 0xFF9A9A9A);

        drawSlots(graphics, x, y);
        drawColumnColours(graphics, x, y);
        drawModeControls(graphics, x, y);
        drawSequentialColumn(graphics, x, y);
    }

    private void drawSlots(GuiGraphics graphics, int x, int y) {
        for (int row = 0; row < SortingMachineBlockEntity.ROWS; row++) {
            for (int column = 0;
                    column < SortingMachineBlockEntity.COLUMNS;
                    column++) {

                drawSlotFrame(
                        graphics,
                        x + 25 + column * 18,
                        y + 17 + row * 18);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotFrame(
                        graphics,
                        x + 7 + column * 18,
                        y + 139 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            drawSlotFrame(
                    graphics,
                    x + 7 + column * 18,
                    y + 197);
        }
    }

    private static void drawSlotFrame(
            GuiGraphics graphics,
            int x,
            int y) {

        graphics.fill(x, y, x + 18, y + 18, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFFFFFFFF);
        graphics.fill(x + 2, y + 2, x + 17, y + 17, 0xFF8B8B8B);
    }

    private void drawColumnColours(
            GuiGraphics graphics,
            int x,
            int y) {

        for (int column = 0;
                column < SortingMachineBlockEntity.COLUMNS;
                column++) {

            drawColourButton(
                    graphics,
                    x + 28 + column * 18,
                    y + 110,
                    menu.getColumnColour(column));
        }
    }

    private void drawModeControls(
            GuiGraphics graphics,
            int x,
            int y) {

        drawButton(
                graphics,
                x + 7,
                y + 73,
                Integer.toString(menu.getPullMode() + 1));
        drawButton(
                graphics,
                x + 7,
                y + 91,
                Integer.toString(menu.getMode() + 1));

        int mode = menu.getMode();
        if (mode == SortingMachineBlockEntity.MODE_ANY_ITEM_DEFAULT
                || mode == SortingMachineBlockEntity.MODE_WHOLE_STACK_DEFAULT) {
            drawColourButton(
                    graphics,
                    x + 7,
                    y + 109,
                    menu.getDefaultColour());
        }

        graphics.drawString(
                font,
                PULL_NAMES[menu.getPullMode()],
                x + 26,
                y + 75,
                0xFF303030,
                false);
        graphics.drawString(
                font,
                MODE_NAMES[menu.getMode()],
                x + 26,
                y + 93,
                0xFF303030,
                false);
    }

    private void drawSequentialColumn(
            GuiGraphics graphics,
            int x,
            int y) {

        if (menu.getMode() > SortingMachineBlockEntity.MODE_ALLSTACK_SEQUENTIAL) {
            return;
        }

        int columnX = x + 24 + menu.getCurrentColumn() * 18;
        graphics.fill(columnX, y + 16, columnX + 2, y + 109, 0xFF36D85A);
        graphics.fill(columnX + 20, y + 16, columnX + 22, y + 109, 0xFF36D85A);
    }

    private void drawButton(
            GuiGraphics graphics,
            int x,
            int y,
            String text) {

        graphics.fill(x, y, x + 14, y + 14, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + 13, y + 13, 0xFFC6C6C6);
        graphics.drawString(font, text, x + 4, y + 3, 0xFF202020, false);
    }

    private void drawColourButton(
            GuiGraphics graphics,
            int x,
            int y,
            int colour) {

        graphics.fill(x, y, x + 12, y + 12, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + 11, y + 11, 0xFFC6C6C6);
        graphics.fill(x + 2, y + 2, x + 10, y + 10, 0xFF6F6F6F);

        if (colour >= 0 && colour < PAINT_COLOURS.length) {
            graphics.fill(
                    x + 4,
                    y + 4,
                    x + 8,
                    y + 8,
                    0xFF000000 | PAINT_COLOURS[colour]);
        } else {
            graphics.fill(x + 4, y + 4, x + 6, y + 6, 0xFFE0E0E0);
            graphics.fill(x + 6, y + 6, x + 8, y + 8, 0xFFE0E0E0);
            graphics.fill(x + 6, y + 4, x + 8, y + 6, 0xFF303030);
            graphics.fill(x + 4, y + 6, x + 6, y + 8, 0xFF303030);
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int mouseButton) {

        int rx = (int) mouseX - getFrame().x();
        int ry = (int) mouseY - getFrame().y();
        boolean forward = mouseButton == 0;

        if (inside(rx, ry, 7, 73, 14, 14)) {
            return sendButton(forward
                    ? SortingMachineMenu.BUTTON_PULL_NEXT
                    : SortingMachineMenu.BUTTON_PULL_PREVIOUS);
        }

        if (inside(rx, ry, 7, 91, 14, 14)) {
            return sendButton(forward
                    ? SortingMachineMenu.BUTTON_MODE_NEXT
                    : SortingMachineMenu.BUTTON_MODE_PREVIOUS);
        }

        for (int column = 0;
                column < SortingMachineBlockEntity.COLUMNS;
                column++) {

            if (inside(rx, ry, 28 + column * 18, 110, 12, 12)) {
                int id = SortingMachineMenu.BUTTON_COLUMN_BASE
                        + column * 2
                        + (forward ? 0 : 1);
                return sendButton(id);
            }
        }

        int mode = menu.getMode();
        if ((mode == SortingMachineBlockEntity.MODE_ANY_ITEM_DEFAULT
                || mode == SortingMachineBlockEntity.MODE_WHOLE_STACK_DEFAULT)
                && inside(rx, ry, 7, 109, 14, 14)) {

            return sendButton(forward
                    ? SortingMachineMenu.BUTTON_DEFAULT_NEXT
                    : SortingMachineMenu.BUTTON_DEFAULT_PREVIOUS);
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

    private static boolean inside(
            int x,
            int y,
            int left,
            int top,
            int width,
            int height) {

        return x >= left
                && x < left + width
                && y >= top
                && y < top + height;
    }
}
