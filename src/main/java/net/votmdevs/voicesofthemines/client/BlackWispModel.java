package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.BlackWispEntity;
import software.bernie.geckolib.model.GeoModel;

public class BlackWispModel extends GeoModel<BlackWispEntity> {
    @Override
    public ResourceLocation getModelResource(BlackWispEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/blackwisp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackWispEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/blackwisp.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlackWispEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/blackwisp_animation.json");
    }
}