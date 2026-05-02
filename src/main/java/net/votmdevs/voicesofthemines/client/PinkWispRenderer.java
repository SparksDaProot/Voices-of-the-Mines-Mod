package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.PinkWispEntity;
import net.votmdevs.voicesofthemines.entity.PinkWispEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PinkWispRenderer extends GeoEntityRenderer<PinkWispEntity> {
    public PinkWispRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PinkWispModel());

        // Создаем ПРАВИЛЬНЫЙ полупрозрачный светящийся слой
        this.addRenderLayer(new GeoRenderLayer<PinkWispEntity>(this) {
            private final ResourceLocation EMISSIVE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/pinkwisp_emissive.png");

            @Override
            public void render(PoseStack poseStack, PinkWispEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                // entityTranslucentEmissive — секрет успеха! Сохраняет прозрачность, но светится в темноте
                RenderType emissiveRenderType = RenderType.entityTranslucentEmissive(EMISSIVE);

                // 15728880 — это константа максимального свечения (LightTexture.FULL_BRIGHT)
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType), partialTick, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        });
    }

    // Включаем поддержку альфа-канала (полупрозрачности) для основной текстуры
    @Override
    public RenderType getRenderType(PinkWispEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}