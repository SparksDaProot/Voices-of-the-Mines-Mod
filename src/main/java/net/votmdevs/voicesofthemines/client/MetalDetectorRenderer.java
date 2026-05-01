package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.item.MetalDetectorItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class MetalDetectorRenderer extends GeoItemRenderer<MetalDetectorItem> {

    public MetalDetectorRenderer() {
        super(new MetalDetectorModel());

        this.addRenderLayer(new GeoRenderLayer<MetalDetectorItem>(this) {
            @Override
            public void render(PoseStack poseStack, MetalDetectorItem animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                ResourceLocation emissiveTexture = ((MetalDetectorModel) getGeoModel()).getEmissiveResource(animatable);

                RenderType glowRenderType = RenderType.eyes(emissiveTexture);
                VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);

                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType, glowBuffer, partialTick, 15728880, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        });
    }
}