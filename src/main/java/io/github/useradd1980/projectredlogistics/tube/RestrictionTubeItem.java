package io.github.useradd1980.projectredlogistics.tube;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.MultipartPlaceContext;
import mrtjp.projectred.expansion.TubeType;
import mrtjp.projectred.expansion.item.TubePartItem;

/**
 * Multipart placement item for the Logistics Restriction Tube.
 *
 * It reuses ProjectRed's pneumatic-tube item renderer while placing the
 * Logistics-specific multipart type.
 */
public final class RestrictionTubeItem extends TubePartItem {

    public RestrictionTubeItem() {
        super(TubeType.PNEUMATIC_TUBE);
    }

    @Override
    public MultiPart newPart(MultipartPlaceContext context) {
        var tube = new RestrictionTubePart();
        return tube.preparePlacement(context) ? tube : null;
    }
}
