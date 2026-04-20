package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.entity.KerfurEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KerfurRenderer extends GeoEntityRenderer<KerfurEntity> {
    public KerfurRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KerfurModel());
        this.shadowRadius = 0.5f;

        this.addRenderLayer(new KerfurEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(KerfurEntity animatable, ResourceLocation texture,
                                    net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                    float partialTick) {
        return RenderType.entityCutout(texture);
    }
}