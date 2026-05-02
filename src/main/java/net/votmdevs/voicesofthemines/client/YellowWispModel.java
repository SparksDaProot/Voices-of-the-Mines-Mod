package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.YellowWispEntity;
import software.bernie.geckolib.model.GeoModel;

public class YellowWispModel extends GeoModel<YellowWispEntity> {
    @Override
    public ResourceLocation getModelResource(YellowWispEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/yellowwisp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(YellowWispEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/yellowwisp.png");
    }

    @Override
    public ResourceLocation getAnimationResource(YellowWispEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/yellowwisp_animation.json");
    }
}