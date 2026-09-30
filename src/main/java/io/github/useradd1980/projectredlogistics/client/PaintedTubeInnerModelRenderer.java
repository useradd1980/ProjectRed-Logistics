package io.github.useradd1980.projectredlogistics.client;

import codechicken.lib.render.CCModel;
import mrtjp.projectred.expansion.client.TubeModelBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds paint-only geometry from ProjectRed's pneumatic tube OBJ.
 *
 * The normal tube model contains the brass/exterior faces first and the inset
 * inner-wall faces last. This renderer extracts only those inner faces, so
 * routing paint colours the inside of the tube while ProjectRed's normal
 * renderer remains responsible for the brass frame.
 */
public final class PaintedTubeInnerModelRenderer {

    private static final Map<String, CCModel> SOURCE_MODELS =
            TubeModelBuilder.loadModels("tube");

    private static final Map<Integer, CCModel> MODEL_CACHE = new HashMap<>();

    private PaintedTubeInnerModelRenderer() { }

    public static CCModel getOrGenerateModel(int connMap) {
        int key = connMap & 0x3F;
        return MODEL_CACHE.computeIfAbsent(key, PaintedTubeInnerModelRenderer::buildModel);
    }

    private static CCModel buildModel(int connMap) {
        int connCount = TubeModelBuilder.countConnections(connMap);
        int axisCount = TubeModelBuilder.countAxis(connMap);

        // Straight-through tubes use ProjectRed's optimized axial model.
        if (connCount == 2 && axisCount == 1) {
            int axis = (connMap & 0x3) != 0
                    ? 0
                    : (connMap & 0xC) != 0 ? 1 : 2;

            return extractTrailingQuads(SOURCE_MODELS.get("a" + axis), 4);
        }

        // Junctions/bends use the centre plus one side model per connection.
        List<CCModel> pieces = new LinkedList<>();
        pieces.add(extractTrailingQuads(SOURCE_MODELS.get("center"), 6));

        for (int side = 0; side < 6; side++) {
            if ((connMap & (1 << side)) != 0) {
                pieces.add(extractTrailingQuads(SOURCE_MODELS.get("s" + side), 4));
            }
        }

        return CCModel.combine(pieces);
    }

    /**
     * ProjectRed's OBJ groups are ordered with exterior/brass faces first and
     * inset inner-wall faces last. Models are quads, so each face is 4 verts.
     */
    private static CCModel extractTrailingQuads(CCModel source, int quadCount) {
        if (source == null) {
            throw new IllegalStateException("Missing ProjectRed tube OBJ group");
        }

        int length = quadCount * 4;
        int start = source.getVertices().length - length;

        if (start < 0 || start % 4 != 0) {
            throw new IllegalStateException(
                    "Unexpected ProjectRed tube OBJ layout: " + source.getVertices().length);
        }

        CCModel result = CCModel.quadModel(length);
        CCModel.copy(source, start, result, 0, length);
        return result;
    }
}
