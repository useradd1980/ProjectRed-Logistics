package io.github.useradd1980.projectredlogistics.client;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.lighting.LightModel;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import mrtjp.projectred.expansion.client.TubeModelBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a second redstone-style internal conductor for routing colour.
 *
 * ProjectRed's own redstone wire occupies one internal corner of the pneumatic
 * tube. This renderer reuses the exact same wire OBJ groups and UVs, but remaps
 * them to the diagonally opposite internal corner. The result is visually
 * consistent with ProjectRed's redstone strip while allowing both redstone and
 * routing colour to coexist in the same tube.
 */
public final class PaintedTubeInnerModelRenderer {

    // ProjectRed's wire OBJ is authored around the model origin and then moved
    // into block-local [0,1] coordinates by its normal tube model loader.
    private static final Map<String, CCModel> SOURCE_MODELS =
            TubeModelBuilder.loadModels(
                    "tube",
                    (name, model) -> model.apply(new Translation(Vector3.CENTER)));

    private static final Map<Integer, CCModel> MODEL_CACHE = new HashMap<>();

    // Existing ProjectRed redstone conductor occupies +X/+Y/+Z:
    //     0.625 .. 0.6875
    // Mirror-sized routing conductor occupies the opposite -X/-Y/-Z corner:
    //     0.3125 .. 0.375
    private static final double NEG_MIN = 0.3125D;
    private static final double NEG_MAX = 0.3750D;

    private PaintedTubeInnerModelRenderer() { }

    public static CCModel getOrGenerateModel(int connMap) {
        int key = connMap & 0x3F;
        return MODEL_CACHE.computeIfAbsent(
                key,
                PaintedTubeInnerModelRenderer::buildModel);
    }

    private static CCModel buildModel(int connMap) {
        int connCount = TubeModelBuilder.countConnections(connMap);
        int axisCount = TubeModelBuilder.countAxis(connMap);

        // ProjectRed uses optimized full-length wire models for straight tubes.
        if (connCount == 2 && axisCount == 1) {
            int axis = (connMap & 0x3) != 0
                    ? 0
                    : (connMap & 0xC) != 0 ? 1 : 2;

            return switch (axis) {
                // Y axis: X/Z are cross-section coordinates.
                case 0 -> remap(
                        SOURCE_MODELS.get("wire_a0"),
                        NEG_MIN, 0.0D, NEG_MIN,
                        NEG_MAX, 1.0D, NEG_MAX);

                // Z axis: X/Y are cross-section coordinates.
                case 1 -> remap(
                        SOURCE_MODELS.get("wire_a1"),
                        NEG_MIN, NEG_MIN, 0.0D,
                        NEG_MAX, NEG_MAX, 1.0D);

                // X axis: Y/Z are cross-section coordinates.
                default -> remap(
                        SOURCE_MODELS.get("wire_a2"),
                        0.0D, NEG_MIN, NEG_MIN,
                        1.0D, NEG_MAX, NEG_MAX);
            };
        }

        // Bends and junctions use a small central conductor cube plus one
        // conductor segment per connected side.
        List<CCModel> pieces = new LinkedList<>();

        pieces.add(remap(
                SOURCE_MODELS.get("wire_center"),
                NEG_MIN, NEG_MIN, NEG_MIN,
                NEG_MAX, NEG_MAX, NEG_MAX));

        for (int side = 0; side < 6; side++) {
            if ((connMap & (1 << side)) == 0) continue;

            pieces.add(switch (side) {
                // Down
                case 0 -> remap(
                        SOURCE_MODELS.get("wire_s0"),
                        NEG_MIN, 0.0D, NEG_MIN,
                        NEG_MAX, NEG_MIN, NEG_MAX);

                // Up
                case 1 -> remap(
                        SOURCE_MODELS.get("wire_s1"),
                        NEG_MIN, NEG_MAX, NEG_MIN,
                        NEG_MAX, 1.0D, NEG_MAX);

                // North
                case 2 -> remap(
                        SOURCE_MODELS.get("wire_s2"),
                        NEG_MIN, NEG_MIN, 0.0D,
                        NEG_MAX, NEG_MAX, NEG_MIN);

                // South
                case 3 -> remap(
                        SOURCE_MODELS.get("wire_s3"),
                        NEG_MIN, NEG_MIN, NEG_MAX,
                        NEG_MAX, NEG_MAX, 1.0D);

                // West
                case 4 -> remap(
                        SOURCE_MODELS.get("wire_s4"),
                        0.0D, NEG_MIN, NEG_MIN,
                        NEG_MIN, NEG_MAX, NEG_MAX);

                // East
                default -> remap(
                        SOURCE_MODELS.get("wire_s5"),
                        NEG_MAX, NEG_MIN, NEG_MIN,
                        1.0D, NEG_MAX, NEG_MAX);
            });
        }

        return CCModel.combine(pieces);
    }

    /**
     * Affinely remaps one of ProjectRed's existing wire OBJ groups into a new
     * block-local bounding box. Vertex ordering, UVs and other attributes are
     * preserved, so the routing strip keeps the same visual texture treatment
     * as ProjectRed's redstone strip.
     */
    private static CCModel remap(
            CCModel source,
            double targetMinX,
            double targetMinY,
            double targetMinZ,
            double targetMaxX,
            double targetMaxY,
            double targetMaxZ) {

        if (source == null) {
            throw new IllegalStateException("Missing ProjectRed wire OBJ group");
        }

        CCModel model = source.copy();

        double sourceMinX = Double.POSITIVE_INFINITY;
        double sourceMinY = Double.POSITIVE_INFINITY;
        double sourceMinZ = Double.POSITIVE_INFINITY;
        double sourceMaxX = Double.NEGATIVE_INFINITY;
        double sourceMaxY = Double.NEGATIVE_INFINITY;
        double sourceMaxZ = Double.NEGATIVE_INFINITY;

        for (var vertex : model.getVertices()) {
            sourceMinX = Math.min(sourceMinX, vertex.vec.x);
            sourceMinY = Math.min(sourceMinY, vertex.vec.y);
            sourceMinZ = Math.min(sourceMinZ, vertex.vec.z);
            sourceMaxX = Math.max(sourceMaxX, vertex.vec.x);
            sourceMaxY = Math.max(sourceMaxY, vertex.vec.y);
            sourceMaxZ = Math.max(sourceMaxZ, vertex.vec.z);
        }

        for (var vertex : model.getVertices()) {
            vertex.vec.x = remapCoordinate(
                    vertex.vec.x,
                    sourceMinX, sourceMaxX,
                    targetMinX, targetMaxX);

            vertex.vec.y = remapCoordinate(
                    vertex.vec.y,
                    sourceMinY, sourceMaxY,
                    targetMinY, targetMaxY);

            vertex.vec.z = remapCoordinate(
                    vertex.vec.z,
                    sourceMinZ, sourceMaxZ,
                    targetMinZ, targetMaxZ);
        }

        // Geometry changed, so refresh lighting information while preserving
        // ProjectRed's original wire UV coordinates.
        model.computeNormals();
        model.computeLighting(LightModel.standardLightModel);

        return model;
    }

    private static double remapCoordinate(
            double value,
            double sourceMin,
            double sourceMax,
            double targetMin,
            double targetMax) {

        double sourceSpan = sourceMax - sourceMin;
        if (sourceSpan == 0.0D) return targetMin;

        double t = (value - sourceMin) / sourceSpan;
        return targetMin + t * (targetMax - targetMin);
    }
}
