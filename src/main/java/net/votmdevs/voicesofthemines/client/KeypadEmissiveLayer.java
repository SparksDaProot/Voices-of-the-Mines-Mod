package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.block.KeypadBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class KeypadEmissiveLayer extends GeoRenderLayer<KeypadBlockEntity> {

    public KeypadEmissiveLayer(GeoRenderer<KeypadBlockEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, KeypadBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        ResourceLocation emissiveTexture = getEmissiveTexture(animatable.getStatus());

        RenderType glowRenderType = RenderType.eyes(emissiveTexture);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);

        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType, glowBuffer, partialTick, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private ResourceLocation getEmissiveTexture(int status) {
        if (status == 0 || status == 2) {
            return new ResourceLocation(KerfurMod.MODID, "textures/block/keypad_write_code_emissive.png");
        }
        else if (status == 3) {
            return new ResourceLocation(KerfurMod.MODID, "textures/block/keypad_error_emissive.png");
        }
        return new ResourceLocation(KerfurMod.MODID, "textures/block/keypad_emissive.png");
    }
}