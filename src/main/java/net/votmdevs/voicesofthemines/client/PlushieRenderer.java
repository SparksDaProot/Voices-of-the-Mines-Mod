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
import net.votmdevs.voicesofthemines.block.PlushieBlockEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PlushieRenderer extends GeoBlockRenderer<PlushieBlockEntity> {

    public PlushieRenderer(BlockEntityRendererProvider.Context context) {
        super(new DefaultedBlockGeoModel<>(new ResourceLocation(VoicesOfTheMines.MODID, "plushie")) {
            @Override
            public ResourceLocation getModelResource(PlushieBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/" + animatable.plushieType.getModelName() + ".geo.json");
            }
            @Override
            public ResourceLocation getTextureResource(PlushieBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/" + animatable.plushieType.getTextureName() + ".png");
            }
            @Override
            public ResourceLocation getAnimationResource(PlushieBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/" + animatable.plushieType.getAnimationFile() + ".json");
            }
        });

        this.addRenderLayer(new GeoRenderLayer<PlushieBlockEntity>(this) {
            @Override
            public void render(PoseStack poseStack, PlushieBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                if (animatable.isTextActive && animatable.plushieType.getSecretText() != null) {
                    String fullText = animatable.plushieType.getSecretText();
                    int maxChars = fullText.length();

                    int visibleChars = Math.min((int)(animatable.textTimer / 2.0F), maxChars);
                    String currentText = fullText.substring(0, visibleChars);

                    int alpha = 255;
                    if (animatable.textTimer > 80) {
                        alpha = (int)(255 * (1.0F - (animatable.textTimer - 80) / 20.0F));
                    }

                    if (alpha > 5) {
                        poseStack.pushPose();

                        poseStack.translate(0.0D, 1.2D, 0.0D);

                        net.minecraft.core.Direction facing = animatable.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
                        float undoRot = switch (facing) {
                            case SOUTH -> -180.0F;
                            case WEST -> -90.0F;
                            case EAST -> -270.0F;
                            default -> 0.0F; // NORTH
                        };
                        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(undoRot));

                        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

                        poseStack.scale(-0.025F, -0.025F, 0.025F);

                        Font font = Minecraft.getInstance().font;
                        float xPos = (float)(-font.width(currentText) / 2); // Идеально центрируем буквы

                        int color = animatable.plushieType.getTextColor().getColor() != null ? animatable.plushieType.getTextColor().getColor() : 0xFFFFFF;
                        int argb = (alpha << 24) | color;

                        font.drawInBatch(currentText, xPos, 0, argb, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

                        poseStack.popPose();
                    }
                }
            }
        });
    }
}