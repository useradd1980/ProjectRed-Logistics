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
}
