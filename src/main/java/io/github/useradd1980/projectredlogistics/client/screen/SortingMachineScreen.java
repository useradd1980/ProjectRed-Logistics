package io.github.useradd1980.projectredlogistics.client.screen;

import io.github.useradd1980.projectredlogistics.block.entity.SortingMachineBlockEntity;
import io.github.useradd1980.projectredlogistics.menu.SortingMachineMenu;
import mrtjp.projectred.lib.GuiLib;
import mrtjp.projectred.lib.Point;
import mrtjp.projectred.redui.RedUIContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SortingMachineScreen
        extends RedUIContainerScreen<SortingMachineMenu> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 222;

    private static final ResourceLocation ICONS =
            ResourceLocation.fromNamespaceAndPath(
                    "projectred_logistics",
                    "textures/gui/sorting_machine_icons.png");

    // Use ProjectRed Expansion's actual Auto Crafter GUI sheet. This gives
    // the Sorting Machine the same window bevel, slot panel styling and
    // power-meter artwork instead of recreating those pixels procedurally.
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(
                    "projectred_expansion",
                    "textures/gui/auto_crafter.png");

    private static final int ICON_SIZE = 14;
    private static final int MODE_ICON_V = 0;
    private static final int PULL_ICON_V = 14;

    private static final int PULL_CONTROL_Y = 78;
    private static final int MODE_CONTROL_Y = 94;
    private static final int DEFAULT_CONTROL_Y = 110;

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
        inventoryLabelX = 8;
        inventoryLabelY = 130;
    }

    @Override
    public void drawBack(
            GuiGraphics graphics,
            Point mouse,
            float partialFrame) {

        int x = getFrame().x();
        int y = getFrame().y();

        drawAutoCrafterWindow(graphics, x, y);

        // Remove the Auto Crafter-specific upper controls. The Sorting
        // Machine redraws the two Auto Crafter meter wells at shifted
        // positions below so they fit beside the 5x8 filter grid.
        graphics.fill(
                x + 5,
                y + 14,
                x + WIDTH - 5,
                y + 129,
                0xFFC6C6C6);

        drawSequentialColumnEmbossed(graphics, x, y);
        drawSlots(graphics, x, y);
        drawPowerIndicators(graphics, x, y);
        drawColumnColours(graphics, x, y);
        drawModeControls(graphics, x, y);
    }

    private void drawSlots(GuiGraphics graphics, int x, int y) {
        boolean sequential =
                menu.getMode() <= SortingMachineBlockEntity.MODE_ALLSTACK_SEQUENTIAL;
        int activeColumn = menu.getCurrentColumn();

        for (int row = 0; row < SortingMachineBlockEntity.ROWS; row++) {
            for (int column = 0;
                    column < SortingMachineBlockEntity.COLUMNS;
                    column++) {

                int interior = sequential && column == activeColumn
                        ? 0xFF777777
                        : 0xFF8B8B8B;

                drawSlotFrame(
                        graphics,
                        x + 25 + column * 18,
                        y + 17 + row * 18,
                        interior);
            }
        }

    }

    private static void drawSlotFrame(
            GuiGraphics graphics,
            int x,
            int y) {

        drawSlotFrame(graphics, x, y, 0xFF8B8B8B);
    }

    private static void drawSlotFrame(
            GuiGraphics graphics,
            int x,
            int y,
            int interiorColour) {

        // Match the vanilla inventory slot bevel exactly:
        // 16x16 interior, dark top/left, white bottom/right.
        graphics.fill(x, y, x + 18, y + 18, interiorColour);

        graphics.fill(x, y, x + 17, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 17, 0xFF373737);

        graphics.fill(x + 17, y + 1, x + 18, y + 17, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
    }

    private void drawPowerIndicators(
            GuiGraphics graphics,
            int x,
            int y) {

        /*
         * Copy the complete recessed meter wells from the Auto Crafter
         * background, but shift both of them nine pixels left so they fit
         * beside the Sorting Machine's filter grid.
         *
         * Auto Crafter source:
         *   first well  x=14..24, live bar at x=16
         *   second well x=25..35, live bar at x=27
         *
         * Sorting Machine destination:
         *   first well  x=5..15,  live bar at x=7
         *   second well x=16..26, live bar at x=18
         */
        graphics.blit(
                BACKGROUND,
                x + 5,
                y + 14,
                14,
                14,
                11,
                62);

        graphics.blit(
                BACKGROUND,
                x + 16,
                y + 14,
                25,
                14,
                11,
                62);

        // Auto Crafter live charge indicator.
        if (menu.canConductorWork()) {
            graphics.blit(
                    BACKGROUND,
                    x + 7,
                    y + 16,
                    177,
                    18,
                    7,
                    9);
        }

        GuiLib.drawVerticalTank(
                graphics,
                BACKGROUND,
                x + 7,
                y + 26,
                177,
                27,
                7,
                48,
                menu.getChargeScaled(48));

        // Auto Crafter live flow indicator.
        if (menu.isFlowFull()) {
            graphics.blit(
                    BACKGROUND,
                    x + 18,
                    y + 16,
                    185,
                    18,
                    7,
                    9);
        }

        GuiLib.drawVerticalTank(
                graphics,
                BACKGROUND,
                x + 18,
                y + 26,
                185,
                27,
                7,
                48,
                menu.getFlowScaled(48));
    }

    private static void drawAutoCrafterWindow(
            GuiGraphics graphics,
            int x,
            int y) {

        /*
         * AutoCrafterScreen draws its window with one background blit:
         *
         * graphics.blit(BACKGROUND, x, y, 0, 0,
         *         getFrame().width(), getFrame().height());
         *
         * Its frame is 176x212 while the Sorting Machine needs 176x222.
         * Keep every Auto Crafter pixel unchanged and insert ten copies of a
         * neutral horizontal row immediately above the inventory section.
         * The lower 92 pixels are then shifted down ten pixels. This preserves
         * the exact ProjectRed bevel and inventory artwork without stretching.
         */
        graphics.blit(
                BACKGROUND,
                x,
                y,
                0,
                0,
                WIDTH,
                120);

        for (int i = 0; i < 10; i++) {
            graphics.blit(
                    BACKGROUND,
                    x,
                    y + 120 + i,
                    0,
                    119,
                    WIDTH,
                    1);
        }

        graphics.blit(
                BACKGROUND,
                x,
                y + 130,
                0,
                120,
                WIDTH,
                92);
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

        // RP2 pr6 sourced these directly from sortmachine.png:
        // pull/automode icons at u=210, v=14*mode
        // sorting-mode icons at u=196, v=14*mode
        // They are repacked into our small helper atlas without alteration.
        drawIconButton(
                graphics,
                x + 7,
                y + PULL_CONTROL_Y,
                menu.getPullMode() * ICON_SIZE,
                PULL_ICON_V);

        drawIconButton(
                graphics,
                x + 7,
                y + MODE_CONTROL_Y,
                menu.getMode() * ICON_SIZE,
                MODE_ICON_V);

        int mode = menu.getMode();
        if (mode == SortingMachineBlockEntity.MODE_ANY_ITEM_DEFAULT
                || mode == SortingMachineBlockEntity.MODE_WHOLE_STACK_DEFAULT) {
            drawColourButton(
                    graphics,
                    x + 7,
                    y + DEFAULT_CONTROL_Y,
                    menu.getDefaultColour());
        }
    }

    private void drawSequentialColumnEmbossed(
            GuiGraphics graphics,
            int x,
            int y) {

        if (menu.getMode() > SortingMachineBlockEntity.MODE_ALLSTACK_SEQUENTIAL) {
            return;
        }

        int activeColumn = menu.getCurrentColumn();
        int slotX = x + 25 + activeColumn * 18;
        int top = y + 17;
        int bottom = y + 107;

        /*
         * RP2-style recessed selection channel.
         *
         * Keep this deliberately vertical: the original GUI reads as a
         * pressed-in column with side bevels, not as a black box around the
         * entire column. The normal slot frames are drawn afterwards.
         *
         * Only draw rails between columns. At the outer edges of the 5x8 grid,
         * an exterior rail creates an unwanted thick bar outside the slots.
         */

        if (activeColumn > 0) {
            // Left recessed rail: dark outer edge, softer mid-tone,
            // light inner lip.
            graphics.fill(
                    slotX - 4,
                    top,
                    slotX - 2,
                    bottom,
                    0xFF343434);
            graphics.fill(
                    slotX - 2,
                    top,
                    slotX - 1,
                    bottom,
                    0xFF666666);
            graphics.fill(
                    slotX - 1,
                    top,
                    slotX,
                    bottom,
                    0xFFC8C8C8);
        }

        if (activeColumn < SortingMachineBlockEntity.COLUMNS - 1) {
            // Right recessed rail mirrors the left side.
            graphics.fill(
                    slotX + 18,
                    top,
                    slotX + 19,
                    bottom,
                    0xFFC8C8C8);
            graphics.fill(
                    slotX + 19,
                    top,
                    slotX + 20,
                    bottom,
                    0xFF666666);
            graphics.fill(
                    slotX + 20,
                    top,
                    slotX + 22,
                    bottom,
                    0xFF343434);
        }

        // Slightly darken the channel behind the active column. The selected
        // slot interiors are also tinted separately in drawSlots().
        graphics.fill(
                slotX,
                top,
                slotX + 18,
                bottom,
                0xFF747474);
    }

    private void drawIconButton(
            GuiGraphics graphics,
            int x,
            int y,
            int u,
            int v) {

        // Keep this GUI sheet at the vanilla 256x256 size so the standard
        // GuiGraphics blit path samples the 14x14 RP2 icon region exactly.
        graphics.blit(
                ICONS,
                x,
                y,
                u,
                v,
                ICON_SIZE,
                ICON_SIZE);
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
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick) {

        super.render(graphics, mouseX, mouseY, partialTick);
        renderControlTooltips(graphics, mouseX, mouseY);
    }

    private void renderControlTooltips(
            GuiGraphics graphics,
            int mouseX,
            int mouseY) {

        int rx = mouseX - getFrame().x();
        int ry = mouseY - getFrame().y();

        if (inside(rx, ry, 7, 16, 7, 58)) {
            graphics.renderTooltip(
                    font,
                    Component.literal(String.format(
                            "Voltage: %.1f V",
                            menu.getConductorCharge() / 10.0)),
                    mouseX,
                    mouseY);
            return;
        }

        if (inside(rx, ry, 18, 16, 7, 58)) {
            graphics.renderTooltip(
                    font,
                    Component.literal("Power flow"),
                    mouseX,
                    mouseY);
            return;
        }

        if (inside(rx, ry, 7, PULL_CONTROL_Y, ICON_SIZE, ICON_SIZE)) {
            graphics.renderTooltip(
                    font,
                    Component.literal(PULL_NAMES[menu.getPullMode()]),
                    mouseX,
                    mouseY);
            return;
        }

        if (inside(rx, ry, 7, MODE_CONTROL_Y, ICON_SIZE, ICON_SIZE)) {
            graphics.renderTooltip(
                    font,
                    Component.literal(MODE_NAMES[menu.getMode()]),
                    mouseX,
                    mouseY);
            return;
        }

        int mode = menu.getMode();
        if ((mode == SortingMachineBlockEntity.MODE_ANY_ITEM_DEFAULT
                || mode == SortingMachineBlockEntity.MODE_WHOLE_STACK_DEFAULT)
                && inside(rx, ry, 7, DEFAULT_CONTROL_Y, 14, 14)) {

            graphics.renderTooltip(
                    font,
                    Component.literal("Default route colour"),
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

        if (inside(rx, ry, 7, PULL_CONTROL_Y, 14, 14)) {
            return sendButton(forward
                    ? SortingMachineMenu.BUTTON_PULL_NEXT
                    : SortingMachineMenu.BUTTON_PULL_PREVIOUS);
        }

        if (inside(rx, ry, 7, MODE_CONTROL_Y, 14, 14)) {
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
                && inside(rx, ry, 7, DEFAULT_CONTROL_Y, 14, 14)) {

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
