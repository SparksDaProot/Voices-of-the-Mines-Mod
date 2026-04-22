package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class TerminalEmissiveLayer extends GeoRenderLayer<VotvTerminalBlockEntity> {
    public TerminalEmissiveLayer(GeoRenderer<VotvTerminalBlockEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, VotvTerminalBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Block block = animatable.getBlockState().getBlock();
        String name = ForgeRegistries.BLOCKS.getKey(block).getPath();
        ResourceLocation texture = new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/" + name + "_emissive.png");

        RenderType glowRenderType = RenderType.eyes(texture);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType, glowBuffer, partialTick, 15728880, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}