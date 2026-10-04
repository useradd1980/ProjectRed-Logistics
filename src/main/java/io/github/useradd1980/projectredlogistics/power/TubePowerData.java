package io.github.useradd1980.projectredlogistics.power;

import io.github.useradd1980.projectredlogistics.ProjectRedLogistics;
import mrtjp.projectred.api.pneumatics.PneumaticTube;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Namespaced marker stored on a pneumatic tube when an Electrotine Alloy
 * conductor has been installed inside it.
 */
public final class TubePowerData {

    public static final ResourceLocation ELECTROTINE_POWER =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectRedLogistics.MOD_ID,
                    "electrotine_power");

    private TubePowerData() { }

    public static boolean isPowered(PneumaticTube tube) {
        return tube.hasData(ELECTROTINE_POWER);
    }

    public static void setPowered(PneumaticTube tube) {
        tube.setData(ELECTROTINE_POWER, new CompoundTag());
    }

    public static void clearPowered(PneumaticTube tube) {
        tube.removeData(ELECTROTINE_POWER);
    }
}
