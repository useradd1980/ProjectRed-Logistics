package io.github.useradd1980.projectredlogistics.routing;

import java.util.OptionalInt;

/**
 * Pure RP2-style colour compatibility rules.
 */
public final class LogisticsColourRules {

    private LogisticsColourRules() { }

    /**
     * Unpainted tubes are neutral. Uncoloured payloads may enter any tube.
     * Coloured payloads may enter unpainted or matching painted tubes only.
     */
    public static boolean canTravel(
            OptionalInt payloadColour,
            OptionalInt tubeColour) {

        return tubeColour.isEmpty()
                || payloadColour.isEmpty()
                || payloadColour.getAsInt() == tubeColour.getAsInt();
    }
}
