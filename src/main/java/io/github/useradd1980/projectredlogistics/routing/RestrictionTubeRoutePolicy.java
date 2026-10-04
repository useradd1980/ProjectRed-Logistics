package io.github.useradd1980.projectredlogistics.routing;

import io.github.useradd1980.projectredlogistics.tube.RestrictionTubePart;
import mrtjp.projectred.api.ProjectRedAPI;
import mrtjp.projectred.api.pneumatics.PneumaticPayload;
import mrtjp.projectred.api.pneumatics.PneumaticRouteContext;
import mrtjp.projectred.api.pneumatics.PneumaticRouteDecision;
import mrtjp.projectred.api.pneumatics.PneumaticRoutePolicy;
import mrtjp.projectred.api.pneumatics.PneumaticTube;

/**
 * Recreates RedPower 2 Restriction Tube routing semantics.
 *
 * RP2 assigned a tube weight of 1,000,000 to a Restriction Tube. That makes
 * the route extremely undesirable without making it unusable when it is the
 * only available path.
 */
public final class RestrictionTubeRoutePolicy implements PneumaticRoutePolicy {

    public static final RestrictionTubeRoutePolicy INSTANCE =
            new RestrictionTubeRoutePolicy();

    public static final int RESTRICTION_COST = 1_000_000;

    private RestrictionTubeRoutePolicy() { }

    @Override
    public PneumaticRouteDecision evaluate(
            PneumaticPayload payload,
            PneumaticRouteContext context) {

        return getTube(context.level(), context.toPos()) instanceof RestrictionTubePart
                ? PneumaticRouteDecision.cost(RESTRICTION_COST)
                : PneumaticRouteDecision.PASS;
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
