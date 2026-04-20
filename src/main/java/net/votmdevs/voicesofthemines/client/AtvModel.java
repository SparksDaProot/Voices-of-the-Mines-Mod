package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.AtvEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AtvModel extends GeoModel<AtvEntity> {
    @Override
    public ResourceLocation getModelResource(AtvEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/atv.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AtvEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/atv.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AtvEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/atv_animation.json");
    }
}