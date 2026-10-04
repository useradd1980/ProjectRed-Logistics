package io.github.useradd1980.projectredlogistics;

import io.github.useradd1980.projectredlogistics.client.ProjectRedLogisticsClient;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import io.github.useradd1980.projectredlogistics.interaction.TubePaintInteractionHandler;
import io.github.useradd1980.projectredlogistics.power.TubePowerData;
import io.github.useradd1980.projectredlogistics.routing.LogisticsColourRoutePolicy;
import io.github.useradd1980.projectredlogistics.routing.LogisticsTubeConnectionPolicy;
import io.github.useradd1980.projectredlogistics.routing.RestrictionTubeRoutePolicy;
import mrtjp.projectred.api.ProjectRedAPI;
import mrtjp.projectred.api.pneumatics.PneumaticTube;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

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
            ProjectRedAPI.expansionAPI.registerPneumaticRoutePolicy(
                    RestrictionTubeRoutePolicy.INSTANCE);
            ProjectRedAPI.expansionAPI.registerPneumaticTubeConnectionPolicy(
                    LogisticsTubeConnectionPolicy.INSTANCE);

            registerPneumaticLowLoadPowerSupport();

            LOGGER.info(
                    "Registered ProjectRed Logistics colour routing, restriction routing, " +
                    "tube connection and electrotine power support");
        });
    }

    /**
     * Patch 0006 adds a generic Predicate<PneumaticTube> registration hook to
     * ProjectRed Expansion. Reflection keeps this addon source compatible with
     * the previous patched API while the local ProjectRed development artifact
     * is being republished.
     */
    private static void registerPneumaticLowLoadPowerSupport() {
        try {
            var method = ProjectRedAPI.expansionAPI
                    .getClass()
                    .getMethod(
                            "registerPneumaticLowLoadPowerPredicate",
                            Predicate.class);

            Predicate<PneumaticTube> predicate =
                    TubePowerData::isPowered;

            method.invoke(
                    ProjectRedAPI.expansionAPI,
                    predicate);

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "ProjectRed Logistics electrotine-powered tubes require " +
                    "upstream patch 0006-generic-pneumatic-low-load-power.patch",
                    e);
        }
    }
}
