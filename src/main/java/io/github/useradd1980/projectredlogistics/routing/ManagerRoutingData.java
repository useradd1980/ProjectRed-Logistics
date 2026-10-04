package io.github.useradd1980.projectredlogistics.routing;

import io.github.useradd1980.projectredlogistics.ProjectRedLogistics;
import mrtjp.projectred.api.pneumatics.PneumaticPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.OptionalInt;

public final class ManagerRoutingData {

    public static final ResourceLocation REQUEST =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectRedLogistics.MOD_ID, "manager_request");

    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String PRIORITY = "priority";

    private ManagerRoutingData() { }

    public static void setRequest(
            PneumaticPayload payload,
            BlockPos source,
            BlockPos target,
            int priority) {

        CompoundTag tag = new CompoundTag();
        tag.putLong(SOURCE, source.asLong());
        tag.putLong(TARGET, target.asLong());
        tag.putInt(PRIORITY, priority);
        payload.setData(REQUEST, tag);
    }

    public static Optional<BlockPos> getSource(PneumaticPayload payload) {
        CompoundTag tag = payload.getData(REQUEST);
        if (tag == null || !tag.contains(SOURCE, Tag.TAG_LONG)) {
            return Optional.empty();
        }
        return Optional.of(BlockPos.of(tag.getLong(SOURCE)));
    }

    public static Optional<BlockPos> getTarget(PneumaticPayload payload) {
        CompoundTag tag = payload.getData(REQUEST);
        if (tag == null || !tag.contains(TARGET, Tag.TAG_LONG)) {
            return Optional.empty();
        }
        return Optional.of(BlockPos.of(tag.getLong(TARGET)));
    }

    public static OptionalInt getPriority(PneumaticPayload payload) {
        CompoundTag tag = payload.getData(REQUEST);
        if (tag == null || !tag.contains(PRIORITY, Tag.TAG_ANY_NUMERIC)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(tag.getInt(PRIORITY));
    }
}
