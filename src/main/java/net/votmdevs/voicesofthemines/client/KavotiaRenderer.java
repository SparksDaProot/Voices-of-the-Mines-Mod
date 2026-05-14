package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.KavotiaEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KavotiaRenderer extends GeoEntityRenderer<KavotiaEntity> {
    public KavotiaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new ResourceLocation(VoicesOfTheMines.MODID, "kavotia")));
        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/turret_emissive.png")));
    }
}