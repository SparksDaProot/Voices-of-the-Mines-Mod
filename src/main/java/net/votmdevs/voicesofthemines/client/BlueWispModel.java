package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.BlueWispEntity;
import software.bernie.geckolib.model.GeoModel;

public class BlueWispModel extends GeoModel<BlueWispEntity> {
    @Override
    public ResourceLocation getModelResource(BlueWispEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/bluewisp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlueWispEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/bluewisp.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlueWispEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/bluewisp_animation.json");
    }
}