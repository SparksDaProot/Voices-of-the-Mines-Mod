package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.RozitalScoutEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RozitalScoutRenderer extends GeoEntityRenderer<RozitalScoutEntity> {
    public RozitalScoutRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new ResourceLocation(VoicesOfTheMines.MODID, "scout")));
        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/scout_emissive.png")));
    }
}