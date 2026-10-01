package io.github.useradd1980.projectredlogistics.routing;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class LogisticsColourRulesTest {

    @Test
    void uncolouredPayloadMayEnterPaintedTube() {
        assertTrue(LogisticsColourRules.canTravel(
                OptionalInt.empty(),
                OptionalInt.of(11)));
    }

    @Test
    void colouredPayloadMayEnterUnpaintedTube() {
        assertTrue(LogisticsColourRules.canTravel(
                OptionalInt.of(5),
                OptionalInt.empty()));
    }

    @Test
    void colouredPayloadMayEnterMatchingFirstTube() {
        assertTrue(LogisticsColourRules.canTravel(
                OptionalInt.of(5),
                OptionalInt.of(5)));
    }

    @Test
    void colouredPayloadIsRejectedByMismatchedFirstTube() {
        assertFalse(LogisticsColourRules.canTravel(
                OptionalInt.of(5),
                OptionalInt.of(11)));
    }
}
