package io.github.useradd1980.projectredlogistics.client;

import codechicken.lib.colour.EnumColour;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.multipart.block.BlockMultipart;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import io.github.useradd1980.projectredlogistics.routing.LogisticsRoutingData;
import mrtjp.projectred.api.ProjectRedAPI;
import mrtjp.projectred.api.pneumatics.PneumaticTube;
import mrtjp.projectred.expansion.client.TubeModelRenderer;
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
import java.util.Iterator;
import java.util.Map;

/**
 * Client-side cache and renderer for painted ProjectRed pneumatic tubes.
 *
 * ProjectRed continues rendering the normal tube. This manager draws a second,
 * slightly expanded translucent copy of the same tube mesh to represent paint.
 */
public final class PaintedTubeClientManager {

    private static final Map<BlockPos, Integer> PAINTED_TUBES = new HashMap<>();

    // Slight expansion prevents the paint pass from z-fighting with ProjectRed's
    // normal tube surface.
    private static final float PAINT_SCALE = 1.003F;

    // CCL colours are packed RGBA. This opacity keeps the original tube details
    // visible underneath the paint layer.
    private static final int PAINT_ALPHA = 0xB0;

    private PaintedTubeClientManager() { }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof Level level) || !level.isClientSide()) {
            return;
        }

        if (event.getChunk() instanceof LevelChunk chunk) {
            scanChunk(level, chunk);
        }
    }

    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;

        ChunkPos chunkPos = event.getChunk().getPos();
        Iterator<BlockPos> it = PAINTED_TUBES.keySet().iterator();

        while (it.hasNext()) {
            BlockPos pos = it.next();
            if (SectionPos.blockToSectionCoord(pos.getX()) == chunkPos.x
                    && SectionPos.blockToSectionCoord(pos.getZ()) == chunkPos.z) {
                it.remove();
            }
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            PAINTED_TUBES.clear();
        }
    }

    /**
     * Predict the player's paint action immediately on the client. The server
     * remains authoritative for persisted metadata and routing.
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()
                || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        var api = ProjectRedAPI.expansionAPI;
        if (api == null) return;

        PneumaticTube tube = api.getPneumaticTube(event.getLevel(), event.getPos());
        if (tube == null) return;

        var hit = BlockMultipart.retracePart(event.getLevel(), event.getPos(), event.getEntity());
        if (hit == null || hit.part != (Object) tube) return;

        ItemStack held = event.getEntity().getItemInHand(event.getHand());

        if (held.getItem() instanceof DyeItem dye) {
            PAINTED_TUBES.put(event.getPos().immutable(), dye.getDyeColor().getId());
        } else if (held.isEmpty() && event.getEntity().isShiftKeyDown()) {
            PAINTED_TUBES.remove(event.getPos());
        }
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES
                || PAINTED_TUBES.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) return;

        Vec3 camera = event.getCamera().getPosition();
        var poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        Iterator<Map.Entry<BlockPos, Integer>> it = PAINTED_TUBES.entrySet().iterator();

        while (it.hasNext()) {
            var entry = it.next();
            BlockPos pos = entry.getKey();

            // Avoid forcing unloaded chunks back into memory.
            if (!level.hasChunk(
                    SectionPos.blockToSectionCoord(pos.getX()),
                    SectionPos.blockToSectionCoord(pos.getZ()))) {
                it.remove();
                continue;
            }

            PneumaticTube tube = ProjectRedAPI.expansionAPI.getPneumaticTube(level, pos);
            if (!(tube instanceof PneumaticTubePart part)) {
                it.remove();
                continue;
            }

            renderPaintedTube(part, entry.getValue(), pos, camera, poseStack, buffers, level);
        }

        buffers.endBatch(RenderType.translucent());
    }

    private static void scanChunk(Level level, LevelChunk chunk) {
        for (BlockPos pos : chunk.getBlockEntities().keySet()) {
            PneumaticTube tube = ProjectRedAPI.expansionAPI == null
                    ? null
                    : ProjectRedAPI.expansionAPI.getPneumaticTube(level, pos);

            if (tube == null) continue;

            var colour = LogisticsRoutingData.getTubeColour(tube);
            if (colour.isPresent()) {
                PAINTED_TUBES.put(pos.immutable(), colour.getAsInt());
            }
        }
    }

    private static void renderPaintedTube(
            PneumaticTubePart part,
            int dyeColour,
            BlockPos pos,
            Vec3 camera,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            MultiBufferSource.BufferSource buffers,
            Level level) {

        // Basic render-distance guard.
        double dx = pos.getX() + 0.5D - camera.x;
        double dy = pos.getY() + 0.5D - camera.y;
        double dz = pos.getZ() + 0.5D - camera.z;
        if (dx * dx + dy * dy + dz * dz > 192.0D * 192.0D) return;

        poseStack.pushPose();
        poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

        // Grow around the block centre, rather than away from the origin.
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(PAINT_SCALE, PAINT_SCALE, PAINT_SCALE);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, pos),
                level.getBrightness(LightLayer.SKY, pos));
        ccrs.overlay = OverlayTexture.NO_OVERLAY;
        ccrs.bind(
                new TransformingVertexConsumer(
                        buffers.getBuffer(RenderType.translucent()),
                        poseStack),
                DefaultVertexFormat.BLOCK);

        int rgba = EnumColour.fromDyeMeta(dyeColour).rgba(PAINT_ALPHA);

        TubeModelRenderer.getOrGeneratePipeModel(part.getConnMap()).render(
                ccrs,
                new IconTransformation(part.getIcon()),
                ColourMultiplier.instance(rgba));

        poseStack.popPose();
    }
}
