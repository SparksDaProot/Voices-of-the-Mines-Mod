package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.entity.OmegaKerfurEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OmegaKerfurRenderer extends GeoEntityRenderer<OmegaKerfurEntity> {
    public OmegaKerfurRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new OmegaKerfurModel());
        this.shadowRadius = 0.7f;
        this.addRenderLayer(new OmegaKerfurEmissiveLayer(this));

        this.addRenderLayer(new OmegaKerfurAccessoryLayer(this));
    }

    @Override
    public RenderType getRenderType(OmegaKerfurEntity animatable, ResourceLocation texture, net.minecraft.client.renderer.MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutout(texture);
    }
}