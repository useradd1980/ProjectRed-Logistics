package io.github.useradd1980.projectredlogistics.client.screen;

import io.github.useradd1980.projectredlogistics.block.entity.ManagerBlockEntity;
import io.github.useradd1980.projectredlogistics.menu.ManagerMenu;
import mrtjp.projectred.lib.GuiLib;
import mrtjp.projectred.lib.Point;
import mrtjp.projectred.redui.RedUIContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ManagerScreen
        extends RedUIContainerScreen<ManagerMenu> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 186;

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(
                    "projectred_expansion",
                    "textures/gui/auto_crafter.png");

    private static final int MODE_X = 153;
    private static final int MODE_Y = 37;
    private static final int PRIORITY_X = 153;
    private static final int PRIORITY_Y = 55;
    private static final int COLOUR_X = 153;
    private static final int COLOUR_Y = 73;
    private static final int CONTROL_SIZE = 14;

    private static final int[] PAINT_COLOURS = {
            0xFFFFFF, 0xFF8000, 0xFF00FF, 0x6C80FF,
            0xFFFF00, 0x00FF00, 0xFF6480, 0x535353,
            0x939393, 0x00FFFF, 0x8000FF, 0x0000FF,
            0x4F2700, 0x008000, 0xFF0000, 0x1F1F1F
    };

    public ManagerScreen(
            ManagerMenu menu,
            Inventory playerInventory,
            Component title) {

        super(WIDTH, HEIGHT, menu, playerInventory, title);
        titleLabelX = 68;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 92;
    }

    @Override
    public void drawBack(
            GuiGraphics graphics,
            Point mouse,
            float partialFrame) {

        int x = getFrame().x();
        int y = getFrame().y();

        drawManagerWindow(graphics, x, y);

        graphics.fill(
                x + 5,
                y + 14,
                x + WIDTH - 5,
                y + 94,
                0xFFC6C6C6);

        drawTemplateSlots(graphics, x, y);
        drawPowerIndicators(graphics, x, y);
        drawControls(graphics, x, y);
    }

    private static void drawManagerWindow(
            GuiGraphics graphics,
            int x,
            int y) {

        graphics.blit(
                BACKGROUND,
                x,
                y,
                0,
                0,
                WIDTH,
                94);

        graphics.blit(
                BACKGROUND,
                x,
                y + 94,
                0,
                120,
                WIDTH,
                92);
    }

    private void drawTemplateSlots(
            GuiGraphics graphics,
            int x,
            int y) {

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 6; column++) {
                drawSlotFrame(
                        graphics,
                        x + 43 + column * 18,
                        y + 17 + row * 18);
            }
        }
    }

    private static void drawSlotFrame(
            GuiGraphics graphics,
            int x,
            int y) {

        graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        graphics.fill(x, y, x + 17, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 17, 0xFF373737);
        graphics.fill(x + 17, y + 1, x + 18, y + 17, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
    }

    private void drawPowerIndicators(
            GuiGraphics graphics,
            int x,
            int y) {

        graphics.blit(
                BACKGROUND,
                x + 14,
                y + 14,
                14,
                14,
                11,
                62);
        graphics.blit(
                BACKGROUND,
                x + 25,
                y + 14,
                25,
                14,
                11,
                62);

        if (menu.canConductorWork()) {
            graphics.blit(
                    BACKGROUND,
                    x + 16,
                    y + 16,
                    177,
                    18,
                    7,
                    9);
        }

        GuiLib.drawVerticalTank(
                graphics,
                BACKGROUND,
                x + 16,
                y + 26,
                177,
                27,
                7,
                48,
                menu.getChargeScaled(48));

        if (menu.isFlowFull()) {
            graphics.blit(
                    BACKGROUND,
                    x + 27,
                    y + 16,
                    185,
                    18,
                    7,
                    9);
        }

        GuiLib.drawVerticalTank(
                graphics,
                BACKGROUND,
                x + 27,
                y + 26,
                185,
                27,
                7,
                48,
                menu.getFlowScaled(48));
    }

    private void drawControls(
            GuiGraphics graphics,
            int x,
            int y) {

        drawControlFrame(graphics, x + MODE_X, y + MODE_Y);
        drawControlFrame(graphics, x + PRIORITY_X, y + PRIORITY_Y);
        drawControlFrame(graphics, x + COLOUR_X, y + COLOUR_Y);

        String modeText =
                menu.getMode() == ManagerBlockEntity.MODE_STOCK
                        ? "S"
                        : "E";
        graphics.drawString(
                font,
                modeText,
                x + MODE_X + 5,
                y + MODE_Y + 3,
                0xFFFFFFFF,
                true);

        String priority = Integer.toString(menu.getPriority());
        graphics.drawString(
                font,
                priority,
                x + PRIORITY_X + 7 - font.width(priority) / 2,
                y + PRIORITY_Y + 3,
                0xFFFFFFFF,
                true);

        int colour = menu.getRouteColour();
        if (colour >= 0 && colour < PAINT_COLOURS.length) {
            graphics.fill(
                    x + COLOUR_X + 5,
                    y + COLOUR_Y + 5,
                    x + COLOUR_X + 9,
                    y + COLOUR_Y + 9,
                    0xFF000000 | PAINT_COLOURS[colour]);
        } else {
            graphics.fill(x + COLOUR_X + 5, y + COLOUR_Y + 5, x + COLOUR_X + 7, y + COLOUR_Y + 7, 0xFFE0E0E0);
            graphics.fill(x + COLOUR_X + 7, y + COLOUR_Y + 7, x + COLOUR_X + 9, y + COLOUR_Y + 9, 0xFFE0E0E0);
            graphics.fill(x + COLOUR_X + 7, y + COLOUR_Y + 5, x + COLOUR_X + 9, y + COLOUR_Y + 7, 0xFF303030);
            graphics.fill(x + COLOUR_X + 5, y + COLOUR_Y + 7, x + COLOUR_X + 7, y + COLOUR_Y + 9, 0xFF303030);
        }
    }

    private static void drawControlFrame(
            GuiGraphics graphics,
            int x,
            int y) {

        graphics.fill(x, y, x + CONTROL_SIZE, y + CONTROL_SIZE, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + CONTROL_SIZE - 1, y + CONTROL_SIZE - 1, 0xFFC6C6C6);
        graphics.fill(x + 3, y + 3, x + CONTROL_SIZE - 3, y + CONTROL_SIZE - 3, 0xFF555555);
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick) {

        super.render(graphics, mouseX, mouseY, partialTick);

        int rx = mouseX - getFrame().x();
        int ry = mouseY - getFrame().y();

        if (inside(rx, ry, 16, 16, 7, 58)) {
            graphics.renderTooltip(
                    font,
                    Component.literal(String.format(
                            "Voltage: %.1f V",
                            menu.getConductorCharge() / 10.0)),
                    mouseX,
                    mouseY);
            return;
        }

        if (inside(rx, ry, 27, 16, 7, 58)) {
            graphics.renderTooltip(
                    font,
                    Component.literal("Power flow"),
                    mouseX,
                    mouseY);
            return;
        }

        if (inside(rx, ry, MODE_X, MODE_Y, CONTROL_SIZE, CONTROL_SIZE)) {
            graphics.renderTooltip(
                    font,
                    Component.literal(
                            menu.getMode() == ManagerBlockEntity.MODE_STOCK
                                    ? "Stock mode"
                                    : "Excess mode"),
                    mouseX,
                    mouseY);
            return;
        }

        if (inside(rx, ry, PRIORITY_X, PRIORITY_Y, CONTROL_SIZE, CONTROL_SIZE)) {
            graphics.renderTooltip(
                    font,
                    Component.literal("Priority: " + menu.getPriority()),
                    mouseX,
                    mouseY);
            return;
        }

        if (inside(rx, ry, COLOUR_X, COLOUR_Y, CONTROL_SIZE, CONTROL_SIZE)) {
            graphics.renderTooltip(
                    font,
                    Component.literal("Routing colour"),
                    mouseX,
                    mouseY);
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

        if (inside(rx, ry, MODE_X, MODE_Y, CONTROL_SIZE, CONTROL_SIZE)) {
            return sendButton(forward
                    ? ManagerMenu.BUTTON_MODE_NEXT
                    : ManagerMenu.BUTTON_MODE_PREVIOUS);
        }

        if (inside(rx, ry, PRIORITY_X, PRIORITY_Y, CONTROL_SIZE, CONTROL_SIZE)) {
            return sendButton(forward
                    ? ManagerMenu.BUTTON_PRIORITY_NEXT
                    : ManagerMenu.BUTTON_PRIORITY_PREVIOUS);
        }

        if (inside(rx, ry, COLOUR_X, COLOUR_Y, CONTROL_SIZE, CONTROL_SIZE)) {
            return sendButton(forward
                    ? ManagerMenu.BUTTON_COLOUR_NEXT
                    : ManagerMenu.BUTTON_COLOUR_PREVIOUS);
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
