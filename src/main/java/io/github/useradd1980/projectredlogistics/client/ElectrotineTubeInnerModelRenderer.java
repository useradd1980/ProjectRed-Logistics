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
 * Builds the Electrotine conductor shown inside an upgraded pneumatic tube.
 *
 * The geometry comes from ProjectRed's pneumatic-tube wire model so the
 * conductor keeps the same thin cross-section right up to every connection.
 * UVs are remapped onto the same central stripe band used by ProjectRed's
 * Low Load Power Line texture.
 */
public final class ElectrotineTubeInnerModelRenderer {

    private static final Map<String, CCModel> SOURCE_MODELS =
            TubeModelBuilder.loadModels(
                    "tube",
                    (name, model) ->
                            model.apply(new Translation(Vector3.CENTER)));

    private static final Map<Integer, CCModel> MODEL_CACHE =
            new HashMap<>();

    // Separate corner from ProjectRed red-alloy (+X/+Y/+Z) and our painted
    // routing strip (-X/-Y/-Z): electrotine uses +X/-Y/+Z.
    private static final double NEG_MIN = 0.3125D;
    private static final double NEG_MAX = 0.3750D;
    private static final double POS_MIN = 0.6250D;
    private static final double POS_MAX = 0.6875D;

    /*
     * ProjectRed's framed Low Load Power Line uses thickness 0 (tw = 1).
     * Its ordinary wire side spans U 7..9 and V 8..24 on the 32x32 texture.
     * Mapping the full signed run from V=8 to V=24 is important: the previous
     * radial mapping mirrored only V=16..24 from the centre to both ends,
     * which is why long straight tube runs appeared almost entirely blue and
     * the yellow stripe showed mainly at endpoints and junctions.
     */
    private static final double WIRE_U = 8.0D / 32.0D;
    private static final double WIRE_V_NEGATIVE = 8.0D / 32.0D;
    private static final double WIRE_V_CENTER = 16.0D / 32.0D;
    private static final double WIRE_V_POSITIVE = 24.0D / 32.0D;

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

        // Straight-through tubes use one full-length conductor model.
        if (connCount == 2 && axisCount == 1) {
            int axis = (connMap & 0x3) != 0
                    ? 0
                    : (connMap & 0xC) != 0 ? 1 : 2;

            return switch (axis) {
                // Y axis
                case 0 -> remap(
                        SOURCE_MODELS.get("wire_a0"),
                        POS_MIN, 0.0D, POS_MIN,
                        POS_MAX, 1.0D, POS_MAX,
                        Axis.Y,
                        WIRE_V_NEGATIVE,
                        WIRE_V_POSITIVE);

                // Z axis
                case 1 -> remap(
                        SOURCE_MODELS.get("wire_a1"),
                        POS_MIN, NEG_MIN, 0.0D,
                        POS_MAX, NEG_MAX, 1.0D,
                        Axis.Z,
                        WIRE_V_NEGATIVE,
                        WIRE_V_POSITIVE);

                // X axis
                default -> remap(
                        SOURCE_MODELS.get("wire_a2"),
                        0.0D, NEG_MIN, POS_MIN,
                        1.0D, NEG_MAX, POS_MAX,
                        Axis.X,
                        WIRE_V_NEGATIVE,
                        WIRE_V_POSITIVE);
            };
        }

        // Bends and junctions use a small centre cube plus one arm per side.
        List<CCModel> pieces = new LinkedList<>();

        pieces.add(remapCentre(
                SOURCE_MODELS.get("wire_center"),
                POS_MIN, NEG_MIN, POS_MIN,
                POS_MAX, NEG_MAX, POS_MAX));

        for (int side = 0; side < 6; side++) {
            if ((connMap & (1 << side)) == 0) continue;

            pieces.add(switch (side) {
                // Down: face -> centre maps 8 -> 16.
                case 0 -> remap(
                        SOURCE_MODELS.get("wire_s0"),
                        POS_MIN, 0.0D, POS_MIN,
                        POS_MAX, NEG_MIN, POS_MAX,
                        Axis.Y,
                        WIRE_V_NEGATIVE,
                        WIRE_V_CENTER);

                // Up: centre -> face maps 16 -> 24.
                case 1 -> remap(
                        SOURCE_MODELS.get("wire_s1"),
                        POS_MIN, NEG_MAX, POS_MIN,
                        POS_MAX, 1.0D, POS_MAX,
                        Axis.Y,
                        WIRE_V_CENTER,
                        WIRE_V_POSITIVE);

                // North
                case 2 -> remap(
                        SOURCE_MODELS.get("wire_s2"),
                        POS_MIN, NEG_MIN, 0.0D,
                        POS_MAX, NEG_MAX, POS_MIN,
                        Axis.Z,
                        WIRE_V_NEGATIVE,
                        WIRE_V_CENTER);

                // South
                case 3 -> remap(
                        SOURCE_MODELS.get("wire_s3"),
                        POS_MIN, NEG_MIN, POS_MAX,
                        POS_MAX, NEG_MAX, 1.0D,
                        Axis.Z,
                        WIRE_V_CENTER,
                        WIRE_V_POSITIVE);

                // West
                case 4 -> remap(
                        SOURCE_MODELS.get("wire_s4"),
                        0.0D, NEG_MIN, POS_MIN,
                        POS_MIN, NEG_MAX, POS_MAX,
                        Axis.X,
                        WIRE_V_NEGATIVE,
                        WIRE_V_CENTER);

                // East
                default -> remap(
                        SOURCE_MODELS.get("wire_s5"),
                        POS_MAX, NEG_MIN, POS_MIN,
                        1.0D, NEG_MAX, POS_MAX,
                        Axis.X,
                        WIRE_V_CENTER,
                        WIRE_V_POSITIVE);
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
            double targetMaxZ,
            Axis stripeAxis,
            double stripeVMin,
            double stripeVMax) {

        if (source == null) {
            throw new IllegalStateException(
                    "Missing ProjectRed tube wire OBJ group");
        }

        CCModel model = source.copy();
        Bounds sourceBounds = Bounds.of(model);

        for (var vertex : model.getVertices()) {
            vertex.vec.x = remapCoordinate(
                    vertex.vec.x,
                    sourceBounds.minX,
                    sourceBounds.maxX,
                    targetMinX,
                    targetMaxX);

            vertex.vec.y = remapCoordinate(
                    vertex.vec.y,
                    sourceBounds.minY,
                    sourceBounds.maxY,
                    targetMinY,
                    targetMaxY);

            vertex.vec.z = remapCoordinate(
                    vertex.vec.z,
                    sourceBounds.minZ,
                    sourceBounds.maxZ,
                    targetMinZ,
                    targetMaxZ);

            double axisValue = switch (stripeAxis) {
                case X -> vertex.vec.x;
                case Y -> vertex.vec.y;
                case Z -> vertex.vec.z;
            };

            double targetAxisMin = switch (stripeAxis) {
                case X -> targetMinX;
                case Y -> targetMinY;
                case Z -> targetMinZ;
            };

            double targetAxisMax = switch (stripeAxis) {
                case X -> targetMaxX;
                case Y -> targetMaxY;
                case Z -> targetMaxZ;
            };

            double t = normalize(
                    axisValue,
                    targetAxisMin,
                    targetAxisMax);

            // Sample ProjectRed's actual low-load wire side band lengthwise.
            vertex.uv.u = WIRE_U;
            vertex.uv.v =
                    stripeVMin
                            + (stripeVMax - stripeVMin) * t;
        }

        model.computeNormals();
        model.computeLighting(LightModel.standardLightModel);
        return model;
    }

    private static CCModel remapCentre(
            CCModel source,
            double targetMinX,
            double targetMinY,
            double targetMinZ,
            double targetMaxX,
            double targetMaxY,
            double targetMaxZ) {

        if (source == null) {
            throw new IllegalStateException(
                    "Missing ProjectRed tube wire centre group");
        }

        CCModel model = source.copy();
        Bounds sourceBounds = Bounds.of(model);

        for (var vertex : model.getVertices()) {
            vertex.vec.x = remapCoordinate(
                    vertex.vec.x,
                    sourceBounds.minX,
                    sourceBounds.maxX,
                    targetMinX,
                    targetMaxX);

            vertex.vec.y = remapCoordinate(
                    vertex.vec.y,
                    sourceBounds.minY,
                    sourceBounds.maxY,
                    targetMinY,
                    targetMaxY);

            vertex.vec.z = remapCoordinate(
                    vertex.vec.z,
                    sourceBounds.minZ,
                    sourceBounds.maxZ,
                    targetMinZ,
                    targetMaxZ);

            // Centre of the same low-load wire side band. Arms on every side
            // meet this phase, so junction transitions remain continuous.
            vertex.uv.u = WIRE_U;
            vertex.uv.v = WIRE_V_CENTER;
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

    private static double normalize(
            double value,
            double min,
            double max) {

        double span = max - min;
        if (span == 0.0D) return 0.0D;

        return Math.max(
                0.0D,
                Math.min(1.0D, (value - min) / span));
    }

    private enum Axis {
        X,
        Y,
        Z
    }

    private record Bounds(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ) {

        private static Bounds of(CCModel model) {
            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double minZ = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;

            for (var vertex : model.getVertices()) {
                minX = Math.min(minX, vertex.vec.x);
                minY = Math.min(minY, vertex.vec.y);
                minZ = Math.min(minZ, vertex.vec.z);
                maxX = Math.max(maxX, vertex.vec.x);
                maxY = Math.max(maxY, vertex.vec.y);
                maxZ = Math.max(maxZ, vertex.vec.z);
            }

            return new Bounds(
                    minX,
                    minY,
                    minZ,
                    maxX,
                    maxY,
                    maxZ);
        }
    }
}
