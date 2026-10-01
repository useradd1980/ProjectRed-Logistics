package io.github.useradd1980.projectredlogistics.client.screen;

import io.github.useradd1980.projectredlogistics.menu.FilterMenu;
import mrtjp.projectred.lib.Point;
import mrtjp.projectred.redui.RedUIContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Functional first-pass Filter screen.
 *
 * Uses Minecraft's dispenser background because its 3x3 slot layout matches
 * the RP2 Filter. A dedicated RP2-inspired ProjectRed Logistics texture and
 * colour selector will replace this once the mechanics are validated.
 */
public class FilterScreen extends RedUIContainerScreen<FilterMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/gui/container/dispenser.png");

    public FilterScreen(
            FilterMenu menu,
            Inventory playerInventory,
            Component title) {

        super(176, 166, menu, playerInventory, title);
        inventoryLabelX = 8;
        inventoryLabelY = 73;
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
    }
}
