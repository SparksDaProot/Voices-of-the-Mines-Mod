package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.PinkWispEntity;
import software.bernie.geckolib.model.GeoModel;

public class PinkWispModel extends GeoModel<PinkWispEntity> {
    @Override
    public ResourceLocation getModelResource(PinkWispEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/pinkwisp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PinkWispEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/pinkwisp.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PinkWispEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/pinkwisp_animation.json");
    }
}