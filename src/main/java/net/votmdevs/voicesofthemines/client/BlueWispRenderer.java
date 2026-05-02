package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.BlueWispEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BlueWispRenderer extends GeoEntityRenderer<BlueWispEntity> {
    public BlueWispRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BlueWispModel());

        this.addRenderLayer(new GeoRenderLayer<BlueWispEntity>(this) {
            private final ResourceLocation EMISSIVE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/bluewisp_emissive.png");

            @Override
            public void render(PoseStack poseStack, BlueWispEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType emissiveRenderType = RenderType.entityTranslucentEmissive(EMISSIVE);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType), partialTick, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        });
    }

    @Override
    public RenderType getRenderType(BlueWispEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}