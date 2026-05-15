package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.TapeRecorderBlockEntity;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class TapeRecorderRenderer extends GeoBlockRenderer<TapeRecorderBlockEntity> {
    public TapeRecorderRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<TapeRecorderBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(TapeRecorderBlockEntity object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/tape_recorder.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(TapeRecorderBlockEntity object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/tape_recorder.png");
            }

            @Override
            public ResourceLocation getAnimationResource(TapeRecorderBlockEntity object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/tape_record_animation.json");
            }
        });

        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/tape_recorder_emissive.png")));

        this.addRenderLayer(new GeoRenderLayer<TapeRecorderBlockEntity>(this) {
            @Override
            public void render(PoseStack poseStack, TapeRecorderBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                if (animatable.textTimer > 0 && animatable.lastMessage != null && !animatable.lastMessage.isEmpty()) {
                    Minecraft mc = Minecraft.getInstance();
                    Font font = mc.font;

                    // fade
                    float alpha = 1.0f;
                    if (animatable.textTimer < 20) {
                        alpha = animatable.textTimer / 20.0f;
                    }
                    int color = (int)(alpha * 255) << 24 | 0xFFFFFF;

                    poseStack.pushPose();

                    poseStack.translate(0.5D, 1.5D, 0.5D);

                    net.minecraft.core.Direction facing = animatable.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
                    float undoRot = switch (facing) {
                        case SOUTH -> -180.0F;
                        case WEST -> -90.0F;
                        case EAST -> -270.0F;
                        default -> 0.0F; // NORTH
                    };
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(undoRot));

                    // Billboard effect
                    poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

                    // size
                    poseStack.scale(-0.025F, -0.025F, 0.025F);

                    Matrix4f matrix4f = poseStack.last().pose();
                    float opacity = mc.options.getBackgroundOpacity(0.25F);
                    int j = (int)(opacity * 255.0F) << 24;

                    float width = (float)(-font.width(animatable.lastMessage) / 2) + 20.0F;

                    font.drawInBatch(animatable.lastMessage, width, 0, color, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, j, packedLight);

                    poseStack.popPose();
                }
            }
        });
    }
}