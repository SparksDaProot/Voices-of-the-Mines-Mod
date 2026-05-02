package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.entity.BlackWispEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BlackWispRenderer extends GeoEntityRenderer<BlackWispEntity> {
    public BlackWispRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BlackWispModel());
    }

    @Override
    public RenderType getRenderType(BlackWispEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}