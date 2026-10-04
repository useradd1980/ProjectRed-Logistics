package io.github.useradd1980.projectredlogistics.client;

import io.github.useradd1980.projectredlogistics.ProjectRedLogistics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public final class RestrictionTubeClientTextures {

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectRedLogistics.MOD_ID,
                    "block/restriction_tube");

    private RestrictionTubeClientTextures() { }

    public static TextureAtlasSprite sprite() {
        return Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(TEXTURE);
    }
}
