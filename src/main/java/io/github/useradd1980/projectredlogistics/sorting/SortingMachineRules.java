package io.github.useradd1980.projectredlogistics.sorting;

/**
 * Pure RedPower 2 pr6 Sorting Machine mode rules.
 *
 * Keeping these decisions outside the block entity makes them directly
 * unit-testable and prevents GUI/power refactors from changing sorter
 * semantics.
 */
public final class SortingMachineRules {

    public static final int MODE_ANYSTACK_SEQUENTIAL = 0;
    public static final int MODE_ALLSTACK_SEQUENTIAL = 1;
    public static final int MODE_RANDOM_ALLSTACK = 2;
    public static final int MODE_ANY_ITEM = 3;
    public static final int MODE_ANY_ITEM_DEFAULT = 4;
    public static final int MODE_ANY_ITEM_WHOLE_STACK = 5;
    public static final int MODE_WHOLE_STACK_DEFAULT = 6;

    // RP2 TileSorter requires at least 60 V and draws 25 power units per item.
    public static final int MIN_OPERATING_CHARGE = 600;
    public static final int POWER_PER_ITEM = 25;

    private SortingMachineRules() { }

    public static boolean hasOperatingPower(int conductorCharge) {
        return conductorCharge > MIN_OPERATING_CHARGE;
    }

    public static int powerCostForItems(int itemCount) {
        return POWER_PER_ITEM * Math.max(0, itemCount);
    }

    public static boolean usesDefaultRoute(int mode) {
        return mode == MODE_ANY_ITEM_DEFAULT
                || mode == MODE_WHOLE_STACK_DEFAULT;
    }

    public static boolean usesWholeStackExtraction(int mode) {
        return mode == MODE_ANY_ITEM_WHOLE_STACK
                || mode == MODE_WHOLE_STACK_DEFAULT;
    }

    public static boolean isSequential(int mode) {
        return mode == MODE_ANYSTACK_SEQUENTIAL
                || mode == MODE_ALLSTACK_SEQUENTIAL;
    }

    public static boolean requiresWholeColumn(int mode) {
        return mode == MODE_ALLSTACK_SEQUENTIAL
                || mode == MODE_RANDOM_ALLSTACK;
    }

    /**
     * RP2 accepts an unmatched travelling payload only when the filter is
     * empty, or when mode 4/6 provides a default route.
     */
    public static boolean acceptsUnmatched(int mode, boolean filterEmpty) {
        return filterEmpty || usesDefaultRoute(mode);
    }

    /**
     * Amount extracted for modes 3-6 after a template has been selected.
     *
     * The original RP2 match stage first required at least configuredCount to
     * be available. Whole-stack modes then collected up to the item's maximum
     * stack size, rather than merely the configured quantity.
     *
     * @return 0 when the configured minimum is not available.
     */
    public static int requestedAmount(
            int mode,
            int configuredCount,
            int availableCount,
            int maxStackSize) {

        if (configuredCount <= 0
                || availableCount < configuredCount
                || maxStackSize <= 0) {
            return 0;
        }

        if (usesWholeStackExtraction(mode)) {
            return Math.min(availableCount, maxStackSize);
        }

        return Math.min(configuredCount, maxStackSize);
    }
}
