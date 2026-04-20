package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.entity.BloodSplashEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BloodSplashRenderer extends GeoEntityRenderer<BloodSplashEntity> {
    public BloodSplashRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BloodSplashModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    public RenderType getRenderType(BloodSplashEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    protected void applyRotations(BloodSplashEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);

        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(animatable.getXRot()));
    }

    @Override
    protected int getBlockLightLevel(BloodSplashEntity entity, BlockPos pos) {
        if (entity.getXRot() >= 170 || entity.getXRot() <= -170) {
            return entity.level().getBrightness(LightLayer.BLOCK, pos.below());
        }

        return super.getBlockLightLevel(entity, pos);
    }
}