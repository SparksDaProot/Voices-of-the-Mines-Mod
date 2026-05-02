package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.YellowWispEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class YellowWispRenderer extends GeoEntityRenderer<YellowWispEntity> {
    public YellowWispRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new YellowWispModel());

        this.addRenderLayer(new GeoRenderLayer<YellowWispEntity>(this) {
            private final ResourceLocation EMISSIVE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/yellowwisp_emissive.png");

            @Override
            public void render(PoseStack poseStack, YellowWispEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType emissiveRenderType = RenderType.entityTranslucentEmissive(EMISSIVE);

                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType), partialTick, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        });
    }

    @Override
    public RenderType getRenderType(YellowWispEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}