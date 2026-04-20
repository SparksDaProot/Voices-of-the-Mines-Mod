package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.entity.OmegaKerfurEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class OmegaKerfurAccessoryLayer extends GeoRenderLayer<OmegaKerfurEntity> {

    private static final ResourceLocation ACCESSORY_TEXTURE = new ResourceLocation(KerfurMod.MODID, "textures/entity/omega_kerfur_accesories.png");

    public OmegaKerfurAccessoryLayer(GeoRenderer<OmegaKerfurEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, OmegaKerfurEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        if (!animatable.getKerfurAccessory().equals("none")) {
            RenderType armorRenderType = RenderType.entityCutoutNoCull(ACCESSORY_TEXTURE);
            VertexConsumer armorBuffer = bufferSource.getBuffer(armorRenderType);

            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, armorRenderType, armorBuffer, partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}