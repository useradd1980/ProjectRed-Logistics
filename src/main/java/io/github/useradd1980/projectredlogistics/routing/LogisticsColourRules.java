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

        return coloursCompatible(payloadColour, tubeColour);
    }

    /**
     * RP2 tube connection rule: unpainted is neutral; two painted tubes only
     * connect when their colours match.
     */
    public static boolean coloursCompatible(
            OptionalInt firstColour,
            OptionalInt secondColour) {

        return firstColour.isEmpty()
                || secondColour.isEmpty()
                || firstColour.getAsInt() == secondColour.getAsInt();
    }
}
