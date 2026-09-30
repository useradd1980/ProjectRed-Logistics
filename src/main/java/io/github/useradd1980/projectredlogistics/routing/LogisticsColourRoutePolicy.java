package io.github.useradd1980.projectredlogistics.routing;

import mrtjp.projectred.api.ProjectRedAPI;
import mrtjp.projectred.api.pneumatics.PneumaticPayload;
import mrtjp.projectred.api.pneumatics.PneumaticRouteContext;
import mrtjp.projectred.api.pneumatics.PneumaticRouteDecision;
import mrtjp.projectred.api.pneumatics.PneumaticRouteNodeContext;
import mrtjp.projectred.api.pneumatics.PneumaticRoutePolicy;
import mrtjp.projectred.api.pneumatics.PneumaticTube;

/**
 * RP2-style colour restrictions implemented entirely as a consumer of the
 * generic ProjectRed pneumatic API.
 *
 * Unpainted tubes are neutral. A painted tube only admits payloads carrying the
 * same colour. Uncoloured payloads therefore cannot enter painted routes.
 */
public final class LogisticsColourRoutePolicy implements PneumaticRoutePolicy {

    public static final LogisticsColourRoutePolicy INSTANCE =
            new LogisticsColourRoutePolicy();

    private LogisticsColourRoutePolicy() { }

    @Override
    public PneumaticRouteDecision evaluate(
            PneumaticPayload payload,
            PneumaticRouteContext context) {

        PneumaticTube tube = getTube(context.level(), context.toPos());
        if (tube == null) {
            return PneumaticRouteDecision.PASS;
        }

        var tubeColour = LogisticsRoutingData.getTubeColour(tube);
        if (tubeColour.isEmpty()) {
            return PneumaticRouteDecision.PASS;
        }

        var payloadColour = LogisticsRoutingData.getPayloadColour(payload);
        if (payloadColour.isPresent()
                && payloadColour.getAsInt() == tubeColour.getAsInt()) {
            return PneumaticRouteDecision.PASS;
        }

        return PneumaticRouteDecision.BLOCK;
    }

    @Override
    public boolean requiresRoutingNode(PneumaticRouteNodeContext context) {
        PneumaticTube tube = getTube(context.level(), context.pos());
        return tube != null && LogisticsRoutingData.getTubeColour(tube).isPresent();
    }

    private static PneumaticTube getTube(
            net.minecraft.world.level.Level level,
            net.minecraft.core.BlockPos pos) {

        var expansionApi = ProjectRedAPI.expansionAPI;
        return expansionApi == null
                ? null
                : expansionApi.getPneumaticTube(level, pos);
    }
}
