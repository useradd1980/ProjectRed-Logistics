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
 * Builds an electrotine conductor strip inside a pneumatic tube.
 *
 * ProjectRed's redstone conductor occupies the positive XYZ corner. The
 * routing-colour strip added by ProjectRed Logistics occupies the negative XYZ
 * corner. Electrotine therefore uses a third, non-overlapping corner:
 * +X / -Y / +Z.
 */
public final class ElectrotineTubeInnerModelRenderer {

    private static final Map<String, CCModel> SOURCE_MODELS =
            TubeModelBuilder.loadModels(
                    "tube",
                    (name, model) ->
                            model.apply(new Translation(Vector3.CENTER)));

    private static final Map<Integer, CCModel> MODEL_CACHE =
            new HashMap<>();

    private static final double NEG_MIN = 0.3125D;
    private static final double NEG_MAX = 0.3750D;
    private static final double POS_MIN = 0.6250D;
    private static final double POS_MAX = 0.6875D;

    private ElectrotineTubeInnerModelRenderer() { }

    public static CCModel getOrGenerateModel(int connMap) {
        int key = connMap & 0x3F;
        return MODEL_CACHE.computeIfAbsent(
                key,
                ElectrotineTubeInnerModelRenderer::buildModel);
    }

    private static CCModel buildModel(int connMap) {
        int connCount = TubeModelBuilder.countConnections(connMap);
        int axisCount = TubeModelBuilder.countAxis(connMap);

        if (connCount == 2 && axisCount == 1) {
            int axis = (connMap & 0x3) != 0
                    ? 0
                    : (connMap & 0xC) != 0 ? 1 : 2;

            return switch (axis) {
                // Y axis: X/Z are cross-section coordinates.
                case 0 -> remap(
                        SOURCE_MODELS.get("wire_a0"),
                        POS_MIN, 0.0D, POS_MIN,
                        POS_MAX, 1.0D, POS_MAX);

                // Z axis: X/Y are cross-section coordinates.
                case 1 -> remap(
                        SOURCE_MODELS.get("wire_a1"),
                        POS_MIN, NEG_MIN, 0.0D,
                        POS_MAX, NEG_MAX, 1.0D);

                // X axis: Y/Z are cross-section coordinates.
                default -> remap(
                        SOURCE_MODELS.get("wire_a2"),
                        0.0D, NEG_MIN, POS_MIN,
                        1.0D, NEG_MAX, POS_MAX);
            };
        }

        List<CCModel> pieces = new LinkedList<>();

        pieces.add(remap(
                SOURCE_MODELS.get("wire_center"),
                POS_MIN, NEG_MIN, POS_MIN,
                POS_MAX, NEG_MAX, POS_MAX));

        for (int side = 0; side < 6; side++) {
            if ((connMap & (1 << side)) == 0) continue;

            pieces.add(switch (side) {
                case 0 -> remap(
                        SOURCE_MODELS.get("wire_s0"),
                        POS_MIN, 0.0D, POS_MIN,
                        POS_MAX, NEG_MIN, POS_MAX);
                case 1 -> remap(
                        SOURCE_MODELS.get("wire_s1"),
                        POS_MIN, NEG_MAX, POS_MIN,
                        POS_MAX, 1.0D, POS_MAX);
                case 2 -> remap(
                        SOURCE_MODELS.get("wire_s2"),
                        POS_MIN, NEG_MIN, 0.0D,
                        POS_MAX, NEG_MAX, POS_MIN);
                case 3 -> remap(
                        SOURCE_MODELS.get("wire_s3"),
                        POS_MIN, NEG_MIN, POS_MAX,
                        POS_MAX, NEG_MAX, 1.0D);
                case 4 -> remap(
                        SOURCE_MODELS.get("wire_s4"),
                        0.0D, NEG_MIN, POS_MIN,
                        POS_MIN, NEG_MAX, POS_MAX);
                default -> remap(
                        SOURCE_MODELS.get("wire_s5"),
                        POS_MAX, NEG_MIN, POS_MIN,
                        1.0D, NEG_MAX, POS_MAX);
            });
        }

        return CCModel.combine(pieces);
    }

    private static CCModel remap(
            CCModel source,
            double targetMinX,
            double targetMinY,
            double targetMinZ,
            double targetMaxX,
            double targetMaxY,
            double targetMaxZ) {

        if (source == null) {
            throw new IllegalStateException(
                    "Missing ProjectRed wire OBJ group");
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
