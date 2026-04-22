package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.DroneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DroneRenderer extends GeoEntityRenderer<DroneEntity> {
    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drone_emissive.png");

    public DroneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DroneModel());
        this.shadowRadius = 0.5f;
        this.addRenderLayer(new GenericEmissiveLayer<>(this, GLOW_TEXTURE));
    }
}