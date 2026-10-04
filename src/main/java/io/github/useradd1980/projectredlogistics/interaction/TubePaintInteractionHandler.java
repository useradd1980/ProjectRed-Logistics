package io.github.useradd1980.projectredlogistics.interaction;

import codechicken.multipart.block.BlockMultipart;
import io.github.useradd1980.projectredlogistics.power.TubePowerData;
import io.github.useradd1980.projectredlogistics.routing.LogisticsRoutingData;
import mrtjp.projectred.api.ProjectRedAPI;
import mrtjp.projectred.api.pneumatics.PneumaticTube;
import mrtjp.projectred.core.PlacementLib;
import mrtjp.projectred.core.init.CoreItems;
import mrtjp.projectred.core.init.CoreTags;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Adds RP2-style material interactions to ProjectRed pneumatic tubes.
 *
 * Controls:
 * - right-click with a DyeItem to paint/repaint the routing channel
 * - right-click with Electrotine Alloy to add a low-load power conductor
 * - sneak + empty-hand right-click clears routing paint first, then removes
 *   the electrotine conductor on a second click
 *
 * CBMultipart performs a fresh ray trace so other parts sharing the same block
 * position are not mistaken for the pneumatic tube.
 */
public final class TubePaintInteractionHandler {

    private TubePaintInteractionHandler() { }

    public static void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event) {

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        var expansionApi = ProjectRedAPI.expansionAPI;
        if (expansionApi == null) return;

        var level = event.getLevel();
        var player = event.getEntity();

        PneumaticTube tube =
                expansionApi.getPneumaticTube(level, event.getPos());
        if (tube == null) return;

        var hit =
                BlockMultipart.retracePart(
                        level,
                        event.getPos(),
                        player);
        if (hit == null || hit.part != (Object) tube) return;

        ItemStack held = player.getItemInHand(event.getHand());

        if (held.is(CoreTags.ELECTROTINE_ALLOY_INGOT_TAG)) {
            cancelSuccessfully(event);

            // Client prediction is handled by PaintedTubeClientManager.
            if (level.isClientSide()) return;

            if (TubePowerData.isPowered(tube)) return;

            TubePowerData.setPowered(tube);

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return;
        }

        if (held.getItem() instanceof DyeItem dye) {
            int colour = dye.getDyeColor().getId();

            cancelSuccessfully(event);

            // Client predicts successful interaction; server owns the actual
            // metadata mutation and inventory change.
            if (level.isClientSide()) return;

            var current =
                    LogisticsRoutingData.getTubeColour(tube);
            if (current.isPresent()
                    && current.getAsInt() == colour) {
                return;
            }

            LogisticsRoutingData.setTubeColour(tube, colour);
            player.displayClientMessage(
                    Component.literal(
                            "Tube painted "
                                    + dye.getDyeColor().getName()),
                    false);

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return;
        }

        if (!held.isEmpty() || !player.isShiftKeyDown()) {
            return;
        }

        // Preserve the existing paint-removal gesture. If both additions are
        // present, the first click removes routing paint and the second removes
        // the electrotine conductor.
        if (LogisticsRoutingData.getTubeColour(tube).isPresent()) {
            cancelSuccessfully(event);

            if (!level.isClientSide()) {
                LogisticsRoutingData.clearTubeColour(tube);
                player.displayClientMessage(
                        Component.literal("Tube paint cleared"),
                        false);
            }
            return;
        }

        if (TubePowerData.isPowered(tube)) {
            cancelSuccessfully(event);

            if (!level.isClientSide()) {
                TubePowerData.clearPowered(tube);

                if (!player.getAbilities().instabuild) {
                    PlacementLib.dropTowardsPlayer(
                            level,
                            event.getPos(),
                            new ItemStack(
                                    CoreItems
                                            .ELECTROTINE_ALLOY_INGOT_ITEM
                                            .get()),
                            player);
                }

            }
        }
    }

    private static void cancelSuccessfully(
            PlayerInteractEvent.RightClickBlock event) {

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
