package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.votmdevs.voicesofthemines.entity.RestockerEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RestockerRenderer extends GeoEntityRenderer<RestockerEntity> {
    public RestockerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RestockerModel());
    }
}