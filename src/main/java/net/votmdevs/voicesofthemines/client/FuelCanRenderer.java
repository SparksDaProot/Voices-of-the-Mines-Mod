package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.entity.FuelCanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FuelCanRenderer extends GeoEntityRenderer<FuelCanEntity> {
    private static final ResourceLocation SELECT_ICON = new ResourceLocation(KerfurMod.MODID, "textures/gui/select.png");

    public FuelCanRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FuelCanModel());
        this.shadowRadius = 0.4f;
    }

    @Override
    public void render(FuelCanEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        Minecraft mc = Minecraft.getInstance();

        if (mc.crosshairPickEntity == entity && !entity.isHeld()) {

            if (mc.player != null && mc.player.distanceTo(entity) < 5.0D) {
                poseStack.pushPose();

                poseStack.translate(0, entity.getBoundingBox().getYsize() / 2.0f, 0);

                poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f));

                float scale = 1.0f;
                poseStack.scale(scale, scale, scale);

                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(SELECT_ICON));
                Matrix4f matrix4f = poseStack.last().pose();
                Matrix3f normalMatrix = poseStack.last().normal();

                vertexConsumer.vertex(matrix4f, -0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, 0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, 0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, -0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();

                poseStack.popPose();
            }
        }
    }
}