package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.DriveEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DriveEmissiveLayer extends GeoRenderLayer<DriveEntity> {
    public DriveEmissiveLayer(GeoRenderer<DriveEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, DriveEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        String sigId = animatable.getEntityData().get(DriveEntity.SIGNAL_ID);
        int sigLevel = animatable.getEntityData().get(DriveEntity.SIGNAL_LEVEL);
        ResourceLocation texture;

        if (sigId == null || sigId.isEmpty()) {
            texture = new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_1_emissive.png");
        } else if (sigLevel == 1) {
            texture = new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_2_stage_1_emissive.png");
        } else if (sigLevel == 2) {
            texture = new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_2_stage_2_emissive.png");
        } else if (sigLevel >= 3) {
            texture = new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_2_stage_3_emissive.png");
        } else {
            texture = new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_2_emissive.png");
        }

        RenderType glowRenderType = RenderType.eyes(texture);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType, glowBuffer, partialTick, 15728880, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}