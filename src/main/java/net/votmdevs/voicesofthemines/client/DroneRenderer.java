package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.entity.DroneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DroneRenderer extends GeoEntityRenderer<DroneEntity> {
    public DroneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DroneModel());
        this.shadowRadius = 0.5f;
    }
}