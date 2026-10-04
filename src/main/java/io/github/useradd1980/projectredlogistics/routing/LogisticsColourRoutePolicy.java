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
 * Unpainted tubes are neutral. Uncoloured payloads may use any tube. Coloured
 * payloads may use unpainted tubes or a tube painted with the same colour, but
 * may not enter a differently coloured tube.
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
        var payloadColour = LogisticsRoutingData.getPayloadColour(payload);

        return LogisticsColourRules.canTravel(payloadColour, tubeColour)
                ? PneumaticRouteDecision.PASS
                : PneumaticRouteDecision.BLOCK;
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
