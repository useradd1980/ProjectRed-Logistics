package io.github.useradd1980.projectredlogistics.client;

import io.github.useradd1980.projectredlogistics.client.screen.FilterScreen;
import io.github.useradd1980.projectredlogistics.client.screen.SortingMachineScreen;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Client-only bootstrap for painted pneumatic-tube visualization.
 */
public final class ProjectRedLogisticsClient {

    private ProjectRedLogisticsClient() { }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ProjectRedLogisticsClient::registerMenuScreens);
        NeoForge.EVENT_BUS.addListener(PaintedTubeClientManager::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(PaintedTubeClientManager::onChunkUnload);
        NeoForge.EVENT_BUS.addListener(PaintedTubeClientManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(
                EventPriority.HIGHEST,
                true,
                PlayerInteractEvent.RightClickBlock.class,
                PaintedTubeClientManager::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(PaintedTubeClientManager::onRenderLevelStage);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(LogisticsContent.FILTER_MENU.get(), FilterScreen::new);
        event.register(
                LogisticsContent.SORTING_MACHINE_MENU.get(),
                SortingMachineScreen::new);
    }
}
