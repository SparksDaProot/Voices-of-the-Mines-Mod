package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.GreenWispEntity;
import software.bernie.geckolib.model.GeoModel;

public class GreenWispModel extends GeoModel<GreenWispEntity> {
    @Override
    public ResourceLocation getModelResource(GreenWispEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/greenwisp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GreenWispEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/greenwisp.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GreenWispEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/greenwisp_animation.json");
    }
}