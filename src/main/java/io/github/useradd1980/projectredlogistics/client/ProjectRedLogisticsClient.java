package io.github.useradd1980.projectredlogistics.client;

import codechicken.multipart.api.MultipartClientRegistry;
import io.github.useradd1980.projectredlogistics.client.screen.BufferScreen;
import io.github.useradd1980.projectredlogistics.client.screen.FilterScreen;
import io.github.useradd1980.projectredlogistics.client.screen.ItemDetectorScreen;
import io.github.useradd1980.projectredlogistics.client.screen.ManagerScreen;
import io.github.useradd1980.projectredlogistics.client.screen.SortingMachineScreen;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Client-only bootstrap for ProjectRed Logistics rendering and screens.
 */
public final class ProjectRedLogisticsClient {

    private ProjectRedLogisticsClient() { }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ProjectRedLogisticsClient::clientSetup);
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

    private static void clientSetup(FMLClientSetupEvent event) {
        MultipartClientRegistry.register(
                LogisticsContent.RESTRICTION_TUBE_PART.get(),
                RestrictionTubePartRenderer.INSTANCE);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(LogisticsContent.BUFFER_MENU.get(), BufferScreen::new);
        event.register(LogisticsContent.FILTER_MENU.get(), FilterScreen::new);
        event.register(
                LogisticsContent.ITEM_DETECTOR_MENU.get(),
                ItemDetectorScreen::new);
        event.register(
                LogisticsContent.SORTING_MACHINE_MENU.get(),
                SortingMachineScreen::new);
        event.register(
                LogisticsContent.MANAGER_MENU.get(),
                ManagerScreen::new);
    }
}
