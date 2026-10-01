package io.github.useradd1980.projectredlogistics.client.screen;

import io.github.useradd1980.projectredlogistics.menu.FilterMenu;
import mrtjp.projectred.lib.Point;
import mrtjp.projectred.redui.RedUIContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * RP2-inspired Filter GUI.
 *
 * Slot and colour-control positions intentionally match the original
 * RedPower 2 pr6 Filter layout recovered from the user's archive:
 * filter grid at (62,17), player inventory at (8,84), hotbar at y=142,
 * and the colour selector at (118,55)..(129,66).
 *
 * The old RP2 texture itself is not redistributed; Minecraft's dispenser
 * background provides the compatible 3x3 layout and the extra Filter
 * controls are drawn by this screen.
 */
public class FilterScreen extends RedUIContainerScreen<FilterMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/gui/container/dispenser.png");

    private static final int COLOUR_X = 118;
    private static final int COLOUR_Y = 55;
    private static final int COLOUR_SIZE = 12;

    // RP2's original display palette, in modern DyeColor ID order:
    // white, orange, magenta, light blue, yellow, lime, pink, gray,
    // light gray, cyan, purple, blue, brown, green, red, black.
    private static final int[] PAINT_COLOURS = {
            0xFFFFFF, 0xFF8000, 0xFF00FF, 0x6C80FF,
            0xFFFF00, 0x00FF00, 0xFF6480, 0x535353,
            0x939393, 0x00FFFF, 0x8000FF, 0x0000FF,
            0x4F2700, 0x008000, 0xFF0000, 0x1F1F1F
    };

    public FilterScreen(
            FilterMenu menu,
            Inventory playerInventory,
            Component title) {

        super(176, 166, menu, playerInventory, title);

        // Match RP2's text placement.
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

        graphics.blit(
                BACKGROUND,
                x,
                y,
                0,
                0,
                getFrame().width(),
                getFrame().height());

        drawFlowArrows(graphics, x, y);
        drawColourSelector(graphics, x, y);
    }

    private void drawFlowArrows(GuiGraphics graphics, int x, int y) {
        // Simple RP2-style left-to-right flow arrows around the 3x3 grid.
        final int colour = 0xFF8A8A8A;

        // Input arrow.
        graphics.fill(x + 31, y + 41, x + 50, y + 44, colour);
        graphics.fill(x + 46, y + 37, x + 50, y + 48, colour);
        graphics.fill(x + 50, y + 40, x + 54, y + 45, colour);

        // Output arrow.
        graphics.fill(x + 122, y + 41, x + 141, y + 44, colour);
        graphics.fill(x + 137, y + 37, x + 141, y + 48, colour);
        graphics.fill(x + 141, y + 40, x + 145, y + 45, colour);
    }

    private void drawColourSelector(GuiGraphics graphics, int x, int y) {
        int sx = x + COLOUR_X;
        int sy = y + COLOUR_Y;

        // Raised-looking 12x12 control frame.
        graphics.fill(sx, sy, sx + COLOUR_SIZE, sy + COLOUR_SIZE, 0xFF373737);
        graphics.fill(sx + 1, sy + 1, sx + COLOUR_SIZE - 1, sy + COLOUR_SIZE - 1, 0xFFC6C6C6);
        graphics.fill(sx + 2, sy + 2, sx + COLOUR_SIZE - 2, sy + COLOUR_SIZE - 2, 0xFF6F6F6F);

        int colourId = menu.getRouteColour();
        if (colourId >= 0 && colourId < PAINT_COLOURS.length) {
            graphics.fill(
                    sx + 4,
                    sy + 4,
                    sx + 8,
                    sy + 8,
                    0xFF000000 | PAINT_COLOURS[colourId]);
        } else {
            // Uncoloured state: small neutral checker, analogous to RP2's
            // special no-paint swatch.
            graphics.fill(sx + 4, sy + 4, sx + 6, sy + 6, 0xFFE0E0E0);
            graphics.fill(sx + 6, sy + 6, sx + 8, sy + 8, 0xFFE0E0E0);
            graphics.fill(sx + 6, sy + 4, sx + 8, sy + 6, 0xFF303030);
            graphics.fill(sx + 4, sy + 6, sx + 6, sy + 8, 0xFF303030);
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int mouseButton) {

        int relativeX = (int) mouseX - getFrame().x();
        int relativeY = (int) mouseY - getFrame().y();

        if (relativeX >= COLOUR_X
                && relativeX < COLOUR_X + COLOUR_SIZE
                && relativeY >= COLOUR_Y
                && relativeY < COLOUR_Y + COLOUR_SIZE) {

            int buttonId = mouseButton == 0
                    ? FilterMenu.BUTTON_COLOUR_NEXT
                    : FilterMenu.BUTTON_COLOUR_PREVIOUS;

            if (minecraft != null
                    && minecraft.player != null
                    && minecraft.gameMode != null
                    && menu.clickMenuButton(minecraft.player, buttonId)) {

                minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId,
                        buttonId);
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
