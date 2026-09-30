package io.github.useradd1980.projectredlogistics.interaction;

import codechicken.multipart.block.BlockMultipart;
import io.github.useradd1980.projectredlogistics.routing.LogisticsRoutingData;
import mrtjp.projectred.api.ProjectRedAPI;
import mrtjp.projectred.api.pneumatics.PneumaticTube;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Adds lightweight RP2-style colour interaction to ProjectRed pneumatic tubes.
 *
 * Current development controls:
 * - right-click the tube with a DyeItem to paint/repaint it
 * - sneak + empty-hand right-click a painted tube to clear its paint
 *
 * CBMultipart performs a fresh ray trace so other parts sharing the same block
 * position are not mistaken for the pneumatic tube.
 */
public final class TubePaintInteractionHandler {

    private TubePaintInteractionHandler() { }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        var expansionApi = ProjectRedAPI.expansionAPI;
        if (expansionApi == null) return;

        var level = event.getLevel();
        var player = event.getEntity();

        PneumaticTube tube = expansionApi.getPneumaticTube(level, event.getPos());
        if (tube == null) return;

        var hit = BlockMultipart.retracePart(level, event.getPos(), player);
        if (hit == null || hit.part != (Object) tube) return;

        ItemStack held = player.getItemInHand(event.getHand());

        if (held.getItem() instanceof DyeItem dye) {
            int colour = dye.getDyeColor().getId();

            cancelSuccessfully(event);

            // Client predicts successful interaction; server owns the actual
            // metadata mutation and inventory change.
            if (level.isClientSide()) return;

            var current = LogisticsRoutingData.getTubeColour(tube);
            if (current.isPresent() && current.getAsInt() == colour) return;

            LogisticsRoutingData.setTubeColour(tube, colour);

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return;
        }

        // Temporary development-friendly way to return a tube to neutral.
        if (held.isEmpty() && player.isShiftKeyDown()
                && LogisticsRoutingData.getTubeColour(tube).isPresent()) {

            cancelSuccessfully(event);

            if (!level.isClientSide()) {
                LogisticsRoutingData.clearTubeColour(tube);
            }
        }
    }

    private static void cancelSuccessfully(PlayerInteractEvent.RightClickBlock event) {
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
