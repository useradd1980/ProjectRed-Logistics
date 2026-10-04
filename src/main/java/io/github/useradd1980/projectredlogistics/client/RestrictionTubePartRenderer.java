package io.github.useradd1980.projectredlogistics.client;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.multipart.api.part.render.PartRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.useradd1980.projectredlogistics.tube.RestrictionTubePart;
import mrtjp.projectred.expansion.TubeType;
import mrtjp.projectred.expansion.client.TubeModelRenderer;
import mrtjp.projectred.expansion.part.PneumaticTubePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;

import javax.annotation.Nullable;

public final class RestrictionTubePartRenderer
        implements PartRenderer<RestrictionTubePart> {

    public static final RestrictionTubePartRenderer INSTANCE =
            new RestrictionTubePartRenderer();

    private static final Vector3[] SIDE_OFFSETS = new Vector3[] {
            new Vector3(0.5, 0.0, 0.5),
            new Vector3(0.5, 1.0, 0.5),
            new Vector3(0.5, 0.5, 0.0),
            new Vector3(0.5, 0.5, 1.0),
            new Vector3(0.0, 0.5, 0.5),
            new Vector3(1.0, 0.5, 0.5)
    };

    private RestrictionTubePartRenderer() { }

    @Override
    public void renderStatic(
            RestrictionTubePart part,
            @Nullable RenderType layer,
            CCRenderState ccrs) {

        if (layer == null
                || (layer == RenderType.cutout() && part.useStaticRenderer())) {
            ccrs.brightness = 0;
            renderTube(ccrs, part);
        }
    }

    @Override
    public void renderDynamic(
            RestrictionTubePart part,
            PoseStack mStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            float partialTicks) {

        if (!part.useStaticRenderer()) {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.brightness = packedLight;
            ccrs.overlay = packedOverlay;
            ccrs.bind(
                    new TransformingVertexConsumer(
                            buffers.getBuffer(RenderType.solid()),
                            mStack),
                    DefaultVertexFormat.BLOCK);
            renderTube(ccrs, part);
        }

        renderPayloadTransport(
                part,
                mStack,
                buffers,
                packedLight,
                packedOverlay,
                partialTicks);
    }

    private static void renderTube(
            CCRenderState ccrs,
            RestrictionTubePart part) {

        int modelKey = part.getConnMap();

        TubeModelRenderer.render(
                ccrs,
                modelKey,
                RestrictionTubeClientTextures.sprite(),
                part.getMaterial(),
                false,
                0);

        if (part.hasRedstone()) {
            int colour = ((part.getSignal() & 0xFF) / 2 + 60) << 24 | 0xFF;
            var projectRedTubeSprite =
                    TubeType.PNEUMATIC_TUBE.getTextures().get(0);

            TubeModelRenderer.getOrGenerateWireModel(modelKey).render(
                    ccrs,
                    new IconTransformation(projectRedTubeSprite),
                    ColourMultiplier.instance(colour));
        }
    }

    private static void renderPayloadTransport(
            RestrictionTubePart part,
            PoseStack mStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            float partialTicks) {

        for (var payload : part.getPneumaticTransport().getPayloads()) {
            boolean halfWay =
                    payload.getProgress()
                            >= PneumaticTubePayload.MAX_PROGRESS / 2;
            float progress =
                    (float) payload.getProgress()
                            / PneumaticTubePayload.MAX_PROGRESS;
            float speed =
                    (float) payload.getSpeed()
                            / PneumaticTubePayload.MAX_PROGRESS;
            float interpolated = progress + speed * partialTicks;

            int direction =
                    halfWay && payload.getOutputSide() != -1
                            ? payload.getOutputSide()
                            : payload.getInputSide() ^ 1;

            int dx = Direction.values()[direction].getStepX();
            int dy = Direction.values()[direction].getStepY();
            int dz = Direction.values()[direction].getStepZ();

            Vector3 pos = SIDE_OFFSETS[direction ^ 1].copy();
            Vector3 dir = new Vector3(dx, dy, dz);
            pos.add(dir.multiply(interpolated));

            mStack.pushPose();
            mStack.translate(pos.x, pos.y, pos.z);
            mStack.scale(0.5f, 0.5f, 0.5f);

            var itemRenderer = Minecraft.getInstance().getItemRenderer();
            var model = itemRenderer.getModel(
                    payload.getItemStack(),
                    part.level(),
                    null,
                    0);

            itemRenderer.render(
                    payload.getItemStack(),
                    ItemDisplayContext.FIXED,
                    false,
                    mStack,
                    buffers,
                    packedLight,
                    packedOverlay,
                    model);

            mStack.popPose();
        }
    }
}
