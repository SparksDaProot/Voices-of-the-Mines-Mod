package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.KerfurEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class KerfurEmissiveLayer extends GeoRenderLayer<KerfurEntity> {
    public KerfurEmissiveLayer(GeoRenderer<KerfurEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, KerfurEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        if (!animatable.isDeactivated() && !animatable.getKerfurColor().equals("abandoned")) {
            ResourceLocation glowTexture = getGlowTextureForColor(animatable.getKerfurColor());
            RenderType eyesRenderType = RenderType.eyes(glowTexture);
            VertexConsumer eyesBuffer = bufferSource.getBuffer(eyesRenderType);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesRenderType, eyesBuffer, partialTick, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private ResourceLocation getGlowTextureForColor(String color) {
        if (color.equals("blue") || color.equals("none")) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/kerfur_emmisive.png");
        }
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/kerfur_" + color + "_emmisive.png");
    }
}