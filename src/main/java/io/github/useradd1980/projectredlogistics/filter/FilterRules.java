package io.github.useradd1980.projectredlogistics.filter;

import io.github.useradd1980.projectredlogistics.routing.LogisticsRoutingData;
import mrtjp.projectred.api.pneumatics.PneumaticPayload;
import net.minecraft.world.item.ItemStack;

/**
 * Deterministic RP2 Filter rules separated from block/world plumbing so they
 * can be unit-tested directly.
 */
public final class FilterRules {

    public static final int NO_COLOUR = -1;

    private FilterRules() { }

    /**
     * RP2 matching ignores configured quantity for an already-travelling
     * payload. Item identity/components must match.
     */
    public static boolean matches(ItemStack template, ItemStack stack) {
        return !template.isEmpty()
                && !stack.isEmpty()
                && ItemStack.isSameItemSameComponents(template, stack);
    }

    /**
     * Replaces the payload's route colour with the Filter's selected colour.
     * NO_COLOUR deliberately clears any colour already carried by the payload.
     */
    public static void applyOutputColour(
            PneumaticPayload payload,
            int routeColour) {

        if (routeColour == NO_COLOUR) {
            LogisticsRoutingData.clearPayloadColour(payload);
            return;
        }

        LogisticsRoutingData.setPayloadColour(payload, routeColour);
    }
}
