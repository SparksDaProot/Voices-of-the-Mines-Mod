package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.block.KeypadBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class KeypadTextLayer extends GeoRenderLayer<KeypadBlockEntity> {

    public KeypadTextLayer(GeoRenderer<KeypadBlockEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, KeypadBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        String code = animatable.getCurrentCode();

        if (code != null && !code.isEmpty()) {
            GeoBone screenBone = bakedModel.getBone("screen").orElse(null);

            if (screenBone != null) {
                poseStack.pushPose();

                poseStack.translate(screenBone.getPosX() / 16.0f, screenBone.getPosY() / 16.0f, screenBone.getPosZ() / 16.0f);

                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180f));
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f));


                float scale = 0.015f;

                int textPixelWidth = Minecraft.getInstance().font.width(code);
                float offsetX = (textPixelWidth * scale) / 2.0f;

                float shiftX = -0.05f;
                float shiftY = -0.83f;
                float shiftZ = 0.35f;
                poseStack.translate(shiftX - offsetX, shiftY, shiftZ);
                poseStack.scale(scale, scale, scale);

                int ledRed = 0xFF0000;
                Minecraft.getInstance().font.drawInBatch(
                        code,
                        0, 0,
                        ledRed,
                        false,
                        poseStack.last().pose(),
                        bufferSource,
                        net.minecraft.client.gui.Font.DisplayMode.NORMAL,
                        0,
                        15728880
                );

                poseStack.popPose();
            }
        }
    }
}