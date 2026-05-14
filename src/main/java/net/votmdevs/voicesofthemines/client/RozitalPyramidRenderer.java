package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.RozitalPyramidEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RozitalPyramidRenderer extends GeoEntityRenderer<RozitalPyramidEntity> {
    public RozitalPyramidRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new ResourceLocation(VoicesOfTheMines.MODID, "cuboid")));
        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/cuboid_emissive.png")));
    }
}