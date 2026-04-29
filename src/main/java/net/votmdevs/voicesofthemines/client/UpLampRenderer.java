package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.UpLampBlock;
import net.votmdevs.voicesofthemines.block.UpLampBlockEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class UpLampRenderer extends GeoBlockRenderer<UpLampBlockEntity> {
    public UpLampRenderer(BlockEntityRendererProvider.Context context) {
        super(new UpLampModel());

        addRenderLayer(new UpLampEmissiveLayer(this));
    }

    private static class UpLampEmissiveLayer extends GeoRenderLayer<UpLampBlockEntity> {
        private static final ResourceLocation EMISSIVE_TEX = new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/uplamp_emissive.png");

        public UpLampEmissiveLayer(GeoBlockRenderer<UpLampBlockEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, UpLampBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

            if (animatable.getBlockState().hasProperty(UpLampBlock.LIT) && animatable.getBlockState().getValue(UpLampBlock.LIT)) {

                RenderType emissiveRenderType = RenderType.eyes(EMISSIVE_TEX);
                VertexConsumer glowBuffer = bufferSource.getBuffer(emissiveRenderType);

                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, glowBuffer, partialTick, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
}