package io.github.useradd1980.projectredlogistics.client;

import codechicken.lib.colour.EnumColour;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.multipart.block.BlockMultipart;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import io.github.useradd1980.projectredlogistics.power.TubePowerData;
import io.github.useradd1980.projectredlogistics.routing.LogisticsRoutingData;
import mrtjp.projectred.api.ProjectRedAPI;
import mrtjp.projectred.api.pneumatics.PneumaticTube;
import mrtjp.projectred.core.init.CoreTags;
import mrtjp.projectred.expansion.part.PneumaticTubePart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Client-side cache and renderer for ProjectRed Logistics tube additions.
 *
 * ProjectRed continues rendering the normal brass tube. Routing paint and the
 * electrotine power conductor are rendered as separate internal wire strips so
 * they can coexist with ProjectRed's own redstone conductor.
 */
public final class PaintedTubeClientManager {

    private static final Map<BlockPos, Integer> PAINTED_TUBES =
            new HashMap<>();

    private static final Set<BlockPos> POWERED_TUBES =
            new HashSet<>();

    /**
     * Representative colour sampled from ProjectRed's Electrotine Alloy
     * ingot artwork. Format is RGBA, matching CCL ColourMultiplier.
     */
    private static final int ELECTROTINE_RGBA = 0x0989CFFF;

    private PaintedTubeClientManager() { }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof Level level)
                || !level.isClientSide()) {
            return;
        }

        if (event.getChunk() instanceof LevelChunk chunk) {
            scanChunk(level, chunk);
        }
    }

    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;

        ChunkPos chunkPos = event.getChunk().getPos();
        removeChunkEntries(PAINTED_TUBES.keySet(), chunkPos);
        removeChunkEntries(POWERED_TUBES, chunkPos);
    }

    private static void removeChunkEntries(
            Set<BlockPos> positions,
            ChunkPos chunkPos) {

        Iterator<BlockPos> it = positions.iterator();

        while (it.hasNext()) {
            BlockPos pos = it.next();

            if (SectionPos.blockToSectionCoord(pos.getX())
                            == chunkPos.x
                    && SectionPos.blockToSectionCoord(pos.getZ())
                            == chunkPos.z) {
                it.remove();
            }
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            PAINTED_TUBES.clear();
            POWERED_TUBES.clear();
        }
    }

    /**
     * Predict local material changes immediately. The server remains
     * authoritative for persisted tube metadata and inventory consumption.
     */
    public static void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event) {

        if (!event.getLevel().isClientSide()
                || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        var api = ProjectRedAPI.expansionAPI;
        if (api == null) return;

        PneumaticTube tube =
                api.getPneumaticTube(
                        event.getLevel(),
                        event.getPos());
        if (tube == null) return;

        var hit = BlockMultipart.retracePart(
                event.getLevel(),
                event.getPos(),
                event.getEntity());
        if (hit == null || hit.part != (Object) tube) return;

        BlockPos pos = event.getPos().immutable();
        ItemStack held =
                event.getEntity().getItemInHand(event.getHand());

        if (held.is(CoreTags.ELECTROTINE_ALLOY_INGOT_TAG)) {
            POWERED_TUBES.add(pos);
            return;
        }

        if (held.getItem() instanceof DyeItem dye) {
            PAINTED_TUBES.put(
                    pos,
                    dye.getDyeColor().getId());
            return;
        }

        if (held.isEmpty()
                && event.getEntity().isShiftKeyDown()) {

            if (PAINTED_TUBES.containsKey(pos)) {
                PAINTED_TUBES.remove(pos);
            } else {
                POWERED_TUBES.remove(pos);
            }
        }
    }

    public static void onRenderLevelStage(
            RenderLevelStageEvent event) {

        if (event.getStage()
                        != RenderLevelStageEvent.Stage.AFTER_PARTICLES
                || (PAINTED_TUBES.isEmpty()
                        && POWERED_TUBES.isEmpty())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) return;

        Vec3 camera = event.getCamera().getPosition();
        var poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers =
                minecraft.renderBuffers().bufferSource();

        renderPaintedTubes(
                level,
                camera,
                poseStack,
                buffers);

        renderPoweredTubes(
                level,
                camera,
                poseStack,
                buffers);

        buffers.endBatch(RenderType.cutout());
    }

    private static void renderPaintedTubes(
            Level level,
            Vec3 camera,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            MultiBufferSource.BufferSource buffers) {

        Iterator<Map.Entry<BlockPos, Integer>> it =
                PAINTED_TUBES.entrySet().iterator();

        while (it.hasNext()) {
            var entry = it.next();
            BlockPos pos = entry.getKey();

            PneumaticTubePart part =
                    resolveLoadedTube(level, pos);
            if (part == null) {
                it.remove();
                continue;
            }

            renderInnerStrip(
                    part,
                    PaintedTubeInnerModelRenderer
                            .getOrGenerateModel(
                                    part.getConnMap()),
                    EnumColour
                            .values()[entry.getValue() & 0xF]
                            .rgba(),
                    pos,
                    camera,
                    poseStack,
                    buffers,
                    level);
        }
    }

    private static void renderPoweredTubes(
            Level level,
            Vec3 camera,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            MultiBufferSource.BufferSource buffers) {

        Iterator<BlockPos> it = POWERED_TUBES.iterator();

        while (it.hasNext()) {
            BlockPos pos = it.next();

            PneumaticTubePart part =
                    resolveLoadedTube(level, pos);
            if (part == null) {
                it.remove();
                continue;
            }

            renderInnerStrip(
                    part,
                    ElectrotineTubeInnerModelRenderer
                            .getOrGenerateModel(
                                    part.getConnMap()),
                    ELECTROTINE_RGBA,
                    pos,
                    camera,
                    poseStack,
                    buffers,
                    level);
        }
    }

    private static PneumaticTubePart resolveLoadedTube(
            Level level,
            BlockPos pos) {

        // Avoid forcing unloaded chunks back into memory.
        if (!level.hasChunk(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ()))) {
            return null;
        }

        PneumaticTube tube =
                ProjectRedAPI.expansionAPI == null
                        ? null
                        : ProjectRedAPI.expansionAPI
                                .getPneumaticTube(level, pos);

        return tube instanceof PneumaticTubePart part
                ? part
                : null;
    }

    private static void scanChunk(
            Level level,
            LevelChunk chunk) {

        for (BlockPos pos : chunk.getBlockEntities().keySet()) {
            PneumaticTube tube =
                    ProjectRedAPI.expansionAPI == null
                            ? null
                            : ProjectRedAPI.expansionAPI
                                    .getPneumaticTube(
                                            level,
                                            pos);

            if (tube == null) continue;

            var colour =
                    LogisticsRoutingData.getTubeColour(tube);
            if (colour.isPresent()) {
                PAINTED_TUBES.put(
                        pos.immutable(),
                        colour.getAsInt());
            }

            if (TubePowerData.isPowered(tube)) {
                POWERED_TUBES.add(pos.immutable());
            }
        }
    }

    private static void renderInnerStrip(
            PneumaticTubePart part,
            codechicken.lib.render.CCModel model,
            int rgba,
            BlockPos pos,
            Vec3 camera,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            MultiBufferSource.BufferSource buffers,
            Level level) {

        double dx = pos.getX() + 0.5D - camera.x;
        double dy = pos.getY() + 0.5D - camera.y;
        double dz = pos.getZ() + 0.5D - camera.z;

        if (dx * dx + dy * dy + dz * dz
                > 192.0D * 192.0D) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(
                pos.getX() - camera.x,
                pos.getY() - camera.y,
                pos.getZ() - camera.z);

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = LightTexture.pack(
                level.getBrightness(
                        LightLayer.BLOCK,
                        pos),
                level.getBrightness(
                        LightLayer.SKY,
                        pos));
        ccrs.overlay = OverlayTexture.NO_OVERLAY;
        ccrs.bind(
                new TransformingVertexConsumer(
                        buffers.getBuffer(
                                RenderType.cutout()),
                        poseStack),
                DefaultVertexFormat.BLOCK);

        model.render(
                ccrs,
                new IconTransformation(part.getIcon()),
                ColourMultiplier.instance(rgba));

        poseStack.popPose();
    }
}
