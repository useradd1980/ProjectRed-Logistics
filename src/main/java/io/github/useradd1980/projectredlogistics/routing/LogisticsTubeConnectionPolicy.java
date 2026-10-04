package io.github.useradd1980.projectredlogistics.routing;

import mrtjp.projectred.api.pneumatics.PneumaticTube;
import mrtjp.projectred.api.pneumatics.PneumaticTubeConnectionPolicy;
import net.minecraft.core.Direction;

/**
 * RP2-style physical tube compatibility.
 *
 * Unpainted tubes connect to any neighbour. Two painted tubes only connect
 * when their route colours match.
 */
public enum LogisticsTubeConnectionPolicy
        implements PneumaticTubeConnectionPolicy {

    INSTANCE;

    @Override
    public boolean canConnect(
            PneumaticTube from,
            PneumaticTube to,
            Direction direction) {

        return LogisticsColourRules.coloursCompatible(
                LogisticsRoutingData.getTubeColour(from),
                LogisticsRoutingData.getTubeColour(to));
    }
}
