package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.AlarmBlockEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class AlarmRenderer extends GeoBlockRenderer<AlarmBlockEntity> {
    public AlarmRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<AlarmBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(AlarmBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/alarm.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(AlarmBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/alarm.png");
            }

            @Override
            public ResourceLocation getAnimationResource(AlarmBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/alarm_animation.json");
            }
        });

        this.addRenderLayer(new net.votmdevs.voicesofthemines.client.GenericEmissiveLayer<>(this,
                new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/alarm_emissive.png")));

        this.addRenderLayer(new GeoRenderLayer<AlarmBlockEntity>(this) {
            @Override
            public void render(PoseStack poseStack, AlarmBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

                if (animatable.iconTimer > 0) {
                    ItemStack displayItem = switch (animatable.detectMode) {
                        case 1 -> new ItemStack(Items.ZOMBIE_HEAD);
                        case 2 -> new ItemStack(Items.PLAYER_HEAD);
                        default -> new ItemStack(Items.REDSTONE);
                    };

                    poseStack.pushPose();

                    poseStack.translate(0.0D, 1.5D, 0.0D);

                    long time = animatable.getLevel().getGameTime();
                    poseStack.mulPose(Axis.YP.rotationDegrees((time + partialTick) * 4.0F));

                    float scale = 0.5F;
                    if (animatable.iconTimer < 10) {
                        scale *= (animatable.iconTimer / 10.0F);
                    }
                    poseStack.scale(scale, scale, scale);

                    Minecraft.getInstance().getItemRenderer().renderStatic(
                            displayItem, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                            poseStack, bufferSource, animatable.getLevel(), 0
                    );

                    poseStack.popPose();
                }
            }
        });
    }

    @Override
    public RenderType getRenderType(AlarmBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}