package io.github.useradd1980.projectredlogistics.client.screen;

import io.github.useradd1980.projectredlogistics.menu.BufferMenu;
import mrtjp.projectred.lib.Point;
import mrtjp.projectred.redui.RedUIContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BufferScreen extends RedUIContainerScreen<BufferMenu> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 186;

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(
                    "projectred_expansion",
                    "textures/gui/auto_crafter.png");

    public BufferScreen(
            BufferMenu menu,
            Inventory playerInventory,
            Component title) {

        super(WIDTH, HEIGHT, menu, playerInventory, title);
        titleLabelX = 70;
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

        graphics.blit(BACKGROUND, x, y, 0, 0, WIDTH, 94);
        graphics.blit(BACKGROUND, x, y + 94, 0, 120, WIDTH, 92);

        graphics.fill(
                x + 5,
                y + 14,
                x + WIDTH - 5,
                y + 94,
                0xFFC6C6C6);

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 5; column++) {
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
}
