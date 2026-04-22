package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.ServerBlock;
import net.votmdevs.voicesofthemines.block.ServerBlockEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ServerEmissiveLayer extends GeoRenderLayer<ServerBlockEntity> {
    public ServerEmissiveLayer(GeoRenderer<ServerBlockEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, ServerBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        boolean isBroken = animatable.getBlockState().getValue(ServerBlock.BROKEN);
        ResourceLocation texture = new ResourceLocation(VoicesOfTheMines.MODID, isBroken ? "textures/block/server_broken_emissive.png" : "textures/block/server_emissive.png");

        RenderType glowRenderType = RenderType.eyes(texture);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType, glowBuffer, partialTick, 15728880, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}