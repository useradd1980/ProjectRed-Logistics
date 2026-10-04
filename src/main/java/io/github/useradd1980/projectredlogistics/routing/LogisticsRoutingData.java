package io.github.useradd1980.projectredlogistics.routing;

import io.github.useradd1980.projectredlogistics.ProjectRedLogistics;
import mrtjp.projectred.api.pneumatics.PneumaticPayload;
import mrtjp.projectred.api.pneumatics.PneumaticTube;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalInt;

/**
 * Encoding used by ProjectRed Logistics for RP2-style routing colours.
 *
 * Colour IDs use Minecraft's standard 0..15 dye ordering. Absence of the
 * namespaced tag means "uncoloured".
 */
public final class LogisticsRoutingData {

    public static final ResourceLocation ROUTE_COLOUR =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectRedLogistics.MOD_ID, "route_colour");

    private static final String COLOUR_FIELD = "colour";
    private static final int MIN_COLOUR = 0;
    private static final int MAX_COLOUR = 15;

    private LogisticsRoutingData() { }

    public static OptionalInt getPayloadColour(PneumaticPayload payload) {
        return readColour(payload.getData(ROUTE_COLOUR));
    }

    public static void setPayloadColour(PneumaticPayload payload, int colour) {
        payload.setData(ROUTE_COLOUR, colourTag(colour));
    }

    public static void clearPayloadColour(PneumaticPayload payload) {
        payload.removeData(ROUTE_COLOUR);
    }

    public static OptionalInt getTubeColour(PneumaticTube tube) {
        return readColour(tube.getData(ROUTE_COLOUR));
    }

    public static void setTubeColour(PneumaticTube tube, int colour) {
        tube.setData(ROUTE_COLOUR, colourTag(colour));
    }

    public static void clearTubeColour(PneumaticTube tube) {
        tube.removeData(ROUTE_COLOUR);
    }

    private static CompoundTag colourTag(int colour) {
        validateColour(colour);

        CompoundTag tag = new CompoundTag();
        tag.putInt(COLOUR_FIELD, colour);
        return tag;
    }

    private static OptionalInt readColour(CompoundTag tag) {
        if (tag == null || !tag.contains(COLOUR_FIELD, Tag.TAG_ANY_NUMERIC)) {
            return OptionalInt.empty();
        }

        int colour = tag.getInt(COLOUR_FIELD);
        return isValidColour(colour)
                ? OptionalInt.of(colour)
                : OptionalInt.empty();
    }

    private static void validateColour(int colour) {
        if (!isValidColour(colour)) {
            throw new IllegalArgumentException(
                    "Routing colour must be between 0 and 15: " + colour);
        }
    }

    private static boolean isValidColour(int colour) {
        return colour >= MIN_COLOUR && colour <= MAX_COLOUR;
    }
}
