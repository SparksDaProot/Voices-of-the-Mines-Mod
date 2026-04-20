package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.block.PosterBlock;
import net.votmdevs.voicesofthemines.block.PosterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class PosterRenderer implements BlockEntityRenderer<PosterBlockEntity> {

    public PosterRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(PosterBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        String url = blockEntity.getCustomImageUrl();
        if (url.isEmpty()) return;

        ResourceLocation customTexture = PosterTextureManager.getTexture(url);
        if (customTexture == null) return;

        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof PosterBlock)) return;

        Direction facing = state.getValue(PosterBlock.FACING);

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        float rotation = 0;
        switch (facing) {
            case NORTH: rotation = 0.0F; break;
            case SOUTH: rotation = 180.0F; break;
            case EAST:  rotation = 270.0F; break;
            case WEST:  rotation = 90.0F; break;
        }
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

        poseStack.translate(-0.5, -0.5, -0.5);
        float minX = -12.0f / 16.0f;
        float maxX = 28.0f / 16.0f;
        float minY = -8.0f / 16.0f;
        float maxY = 32.0f / 16.0f;

        float z = 15.74f / 16.0f;

        float nx = 0.0f, ny = 0.0f, nz = -1.0f;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(customTexture));
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        consumer.vertex(matrix4f, maxX, minY, z).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(packedOverlay).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();

        consumer.vertex(matrix4f, maxX, maxY, z).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(packedOverlay).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();

        consumer.vertex(matrix4f, minX, maxY, z).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(packedOverlay).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();

        consumer.vertex(matrix4f, minX, minY, z).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(packedOverlay).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();

        poseStack.popPose();
    }
}