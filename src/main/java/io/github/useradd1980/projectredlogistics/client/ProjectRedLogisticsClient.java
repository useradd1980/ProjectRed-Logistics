package io.github.useradd1980.projectredlogistics.client;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Client-only bootstrap for painted pneumatic-tube visualization.
 */
public final class ProjectRedLogisticsClient {

    private ProjectRedLogisticsClient() { }

    public static void init() {
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
}
