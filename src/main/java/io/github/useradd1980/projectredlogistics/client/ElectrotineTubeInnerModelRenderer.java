package io.github.useradd1980.projectredlogistics.client;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.lighting.LightModel;
import mrtjp.projectred.transmission.client.FramedWireModelRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds the Electrotine conductor shown inside an upgraded pneumatic tube.
 *
 * The first version reused the pneumatic tube's redstone-wire OBJ geometry.
 * That geometry has UVs for the tube texture, not for ProjectRed
 * Transmission's low_load_power_wire.png. Applying the Low Load Power Line
 * texture to those UVs sampled transparent/edge regions and produced the
 * broken, edge-on-looking line.
 *
 * This version starts with ProjectRed's actual framed Low Load Power Line wire
 * model, preserving its native UV layout exactly, then remaps only the vertex
 * positions into one corner of the pneumatic tube. The stripe artwork is
 * therefore sampled lengthwise exactly as it is on a real Low Load Power Line.
 */
public final class ElectrotineTubeInnerModelRenderer {

    private static final Map<Integer, CCModel> MODEL_CACHE =
            new HashMap<>();

    // Keep the electrotine conductor in its own corner:
    // +X / -Y / +Z.
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
        /*
         * Thickness zero is ProjectRed's thinnest framed-wire geometry.
         * Its UVs are authored specifically for the same
         * low_load_power_wire.png texture that we render on the tube.
         *
         * FramedWireModelRenderer's key packs thickness above bit 5, so for
         * thickness zero the model key is simply the six-bit connection map.
         */
        CCModel model =
                FramedWireModelRenderer
                        .getOrGenerateWireModel(connMap & 0x3F)
                        .copy();

        Bounds bounds = Bounds.of(model);

        remapAxis(
                model,
                Axis.X,
                bounds.minX,
                bounds.maxX,
                targetRange(
                        (connMap & (1 << 4)) != 0,
                        (connMap & (1 << 5)) != 0,
                        POS_MIN,
                        POS_MAX));

        remapAxis(
                model,
                Axis.Y,
                bounds.minY,
                bounds.maxY,
                targetRange(
                        (connMap & (1 << 0)) != 0,
                        (connMap & (1 << 1)) != 0,
                        NEG_MIN,
                        NEG_MAX));

        remapAxis(
                model,
                Axis.Z,
                bounds.minZ,
                bounds.maxZ,
                targetRange(
                        (connMap & (1 << 2)) != 0,
                        (connMap & (1 << 3)) != 0,
                        POS_MIN,
                        POS_MAX));

        model.computeNormals();
        model.computeLighting(LightModel.standardLightModel);
        return model;
    }

    /**
     * Maps one model axis while keeping the conductor centred on the chosen
     * tube corner. A connected end still reaches the corresponding block face;
     * an unconnected axis is collapsed down to the conductor's cross-section.
     */
    private static Range targetRange(
            boolean negativeConnection,
            boolean positiveConnection,
            double crossMin,
            double crossMax) {

        if (negativeConnection && positiveConnection) {
            return new Range(0.0D, 1.0D);
        }

        if (negativeConnection) {
            return new Range(0.0D, crossMax);
        }

        if (positiveConnection) {
            return new Range(crossMin, 1.0D);
        }

        return new Range(crossMin, crossMax);
    }

    private static void remapAxis(
            CCModel model,
            Axis axis,
            double sourceMin,
            double sourceMax,
            Range target) {

        for (var vertex : model.getVertices()) {
            double value = switch (axis) {
                case X -> vertex.vec.x;
                case Y -> vertex.vec.y;
                case Z -> vertex.vec.z;
            };

            double remapped = remapCoordinate(
                    value,
                    sourceMin,
                    sourceMax,
                    target.min,
                    target.max);

            switch (axis) {
                case X -> vertex.vec.x = remapped;
                case Y -> vertex.vec.y = remapped;
                case Z -> vertex.vec.z = remapped;
            }
        }
    }

    private static double remapCoordinate(
            double value,
            double sourceMin,
            double sourceMax,
            double targetMin,
            double targetMax) {

        double sourceSpan = sourceMax - sourceMin;
        if (sourceSpan == 0.0D) {
            return (targetMin + targetMax) * 0.5D;
        }

        double t = (value - sourceMin) / sourceSpan;
        return targetMin + t * (targetMax - targetMin);
    }

    private enum Axis {
        X,
        Y,
        Z
    }

    private record Range(double min, double max) { }

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
