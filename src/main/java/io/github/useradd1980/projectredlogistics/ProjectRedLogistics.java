package io.github.useradd1980.projectredlogistics;

import io.github.useradd1980.projectredlogistics.client.ProjectRedLogisticsClient;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import io.github.useradd1980.projectredlogistics.interaction.TubePaintInteractionHandler;
import io.github.useradd1980.projectredlogistics.routing.LogisticsColourRoutePolicy;
import io.github.useradd1980.projectredlogistics.routing.LogisticsTubeConnectionPolicy;
import mrtjp.projectred.api.ProjectRedAPI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ProjectRedLogistics.MOD_ID)
public final class ProjectRedLogistics {

    public static final String MOD_ID = "projectred_logistics";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ProjectRedLogistics(ModContainer container, IEventBus modEventBus) {
        LogisticsContent.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(TubePaintInteractionHandler::onRightClickBlock);

        if (FMLEnvironment.dist.isClient()) {
            ProjectRedLogisticsClient.init(modEventBus);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        LogisticsContent.registerCapabilities(event);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ProjectRedAPI.expansionAPI == null) {
                throw new IllegalStateException(
                        "ProjectRed Expansion API is unavailable. " +
                        "ProjectRed Logistics requires ProjectRed Expansion.");
            }

            ProjectRedAPI.expansionAPI.registerPneumaticRoutePolicy(
                    LogisticsColourRoutePolicy.INSTANCE);
            ProjectRedAPI.expansionAPI.registerPneumaticTubeConnectionPolicy(
                    LogisticsTubeConnectionPolicy.INSTANCE);

            LOGGER.info(
                    "Registered ProjectRed Logistics colour routing and tube connection policies");
        });
    }
}
