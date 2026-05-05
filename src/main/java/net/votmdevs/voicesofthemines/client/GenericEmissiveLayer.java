package net.votmdevs.voicesofthemines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.IPowerableDevice;
import net.votmdevs.voicesofthemines.block.TransformerBlockEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GenericEmissiveLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private final ResourceLocation emissiveTexture;

    public GenericEmissiveLayer(GeoRenderer<T> entityRendererIn, ResourceLocation emissiveTexture) {
        super(entityRendererIn);
        this.emissiveTexture = emissiveTexture;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        if (animatable instanceof TransformerBlockEntity transformer) {
            boolean isOn = transformer.isMain ? transformer.isActive : (transformer.mainTransformerPos != null && !transformer.needsReboot && transformer.isNetworkActive);
            if (!isOn) return;
        }

        if (animatable instanceof IPowerableDevice device) {
            if (!device.isPowered()) return;
        }

        RenderType glowRenderType = RenderType.eyes(this.emissiveTexture);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType, glowBuffer, partialTick, 15728880, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}