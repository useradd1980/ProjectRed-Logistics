package io.github.useradd1980.projectredlogistics.sorting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SortingMachineRulesTest {

    @Test
    void onlyModesFourAndSixUseDefaultRoute() {
        assertFalse(SortingMachineRules.usesDefaultRoute(0));
        assertFalse(SortingMachineRules.usesDefaultRoute(3));
        assertTrue(SortingMachineRules.usesDefaultRoute(4));
        assertFalse(SortingMachineRules.usesDefaultRoute(5));
        assertTrue(SortingMachineRules.usesDefaultRoute(6));
    }

    @Test
    void onlyModesFiveAndSixUseWholeStackExtraction() {
        assertFalse(SortingMachineRules.usesWholeStackExtraction(3));
        assertFalse(SortingMachineRules.usesWholeStackExtraction(4));
        assertTrue(SortingMachineRules.usesWholeStackExtraction(5));
        assertTrue(SortingMachineRules.usesWholeStackExtraction(6));
    }

    @Test
    void configuredModeExtractsConfiguredQuantity() {
        assertEquals(
                4,
                SortingMachineRules.requestedAmount(
                        SortingMachineRules.MODE_ANY_ITEM,
                        4,
                        32,
                        64));
    }

    @Test
    void wholeStackModeExtractsAllAvailableUpToStackLimit() {
        assertEquals(
                32,
                SortingMachineRules.requestedAmount(
                        SortingMachineRules.MODE_ANY_ITEM_WHOLE_STACK,
                        4,
                        32,
                        64));

        assertEquals(
                64,
                SortingMachineRules.requestedAmount(
                        SortingMachineRules.MODE_ANY_ITEM_WHOLE_STACK,
                        4,
                        96,
                        64));
    }

    @Test
    void wholeStackModeStillRequiresConfiguredMinimum() {
        assertEquals(
                0,
                SortingMachineRules.requestedAmount(
                        SortingMachineRules.MODE_ANY_ITEM_WHOLE_STACK,
                        4,
                        3,
                        64));
    }

    @Test
    void unmatchedInlinePayloadNeedsEmptyFilterOrDefaultMode() {
        assertTrue(SortingMachineRules.acceptsUnmatched(
                SortingMachineRules.MODE_ANY_ITEM,
                true));

        assertFalse(SortingMachineRules.acceptsUnmatched(
                SortingMachineRules.MODE_ANY_ITEM,
                false));

        assertTrue(SortingMachineRules.acceptsUnmatched(
                SortingMachineRules.MODE_ANY_ITEM_DEFAULT,
                false));

        assertTrue(SortingMachineRules.acceptsUnmatched(
                SortingMachineRules.MODE_WHOLE_STACK_DEFAULT,
                false));
    }

    @Test
    void sequentialAndWholeColumnFamiliesMatchPr6Modes() {
        assertTrue(SortingMachineRules.isSequential(0));
        assertTrue(SortingMachineRules.isSequential(1));
        assertFalse(SortingMachineRules.isSequential(2));

        assertFalse(SortingMachineRules.requiresWholeColumn(0));
        assertTrue(SortingMachineRules.requiresWholeColumn(1));
        assertTrue(SortingMachineRules.requiresWholeColumn(2));
        assertFalse(SortingMachineRules.requiresWholeColumn(3));
    }
    @Test
    void rp2PowerThresholdIsStrictlyAboveSixtyVolts() {
        assertFalse(SortingMachineRules.hasOperatingPower(599));
        assertFalse(SortingMachineRules.hasOperatingPower(600));
        assertTrue(SortingMachineRules.hasOperatingPower(601));
    }

    @Test
    void rp2PowerCostIsTwentyFivePerItem() {
        assertEquals(25, SortingMachineRules.powerCostForItems(1));
        assertEquals(100, SortingMachineRules.powerCostForItems(4));
        assertEquals(1600, SortingMachineRules.powerCostForItems(64));
        assertEquals(0, SortingMachineRules.powerCostForItems(0));
    }

}
