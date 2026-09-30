package io.github.useradd1980.projectredlogistics;

import io.github.useradd1980.projectredlogistics.routing.LogisticsColourRoutePolicy;
import mrtjp.projectred.api.ProjectRedAPI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ProjectRedLogistics.MOD_ID)
public final class ProjectRedLogistics {

    public static final String MOD_ID = "projectred_logistics";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ProjectRedLogistics(ModContainer container, IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
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

            LOGGER.info("Registered ProjectRed Logistics colour routing policy");
        });
    }
}
