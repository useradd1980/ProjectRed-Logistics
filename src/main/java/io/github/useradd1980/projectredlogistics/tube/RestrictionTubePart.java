package io.github.useradd1980.projectredlogistics.tube;

import codechicken.multipart.api.MultipartType;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import mrtjp.projectred.expansion.TubeType;
import mrtjp.projectred.expansion.part.PneumaticTubePart;
import net.minecraft.world.item.ItemStack;

/**
 * RP2-style Restriction Tube.
 *
 * Routing behaviour is supplied by {@code RestrictionTubeRoutePolicy}; the
 * part itself remains a normal ProjectRed pneumatic transport container.
 */
public final class RestrictionTubePart extends PneumaticTubePart {

    public RestrictionTubePart() {
        super(TubeType.PNEUMATIC_TUBE);
    }

    @Override
    public MultipartType<?> getType() {
        return LogisticsContent.RESTRICTION_TUBE_PART.get();
    }

    @Override
    protected ItemStack getItem() {
        return new ItemStack(LogisticsContent.RESTRICTION_TUBE_ITEM.get());
    }

    /**
     * A Restriction Tube must always survive ProjectRed's graph compression.
     *
     * Its routing penalty is attached to this exact tube location. If this
     * part were allowed to become a redundant node, the route policy could
     * never see it and the 1,000,000-point RP2 routing penalty would be lost.
     *
     * Declaring the dedicated part active directly is also more robust across
     * chunk/world reloads than rediscovering the part through the public API
     * while the graph itself is being rebuilt.
     */
    @Override
    public boolean requiresActiveNode() {
        return true;
    }
}
