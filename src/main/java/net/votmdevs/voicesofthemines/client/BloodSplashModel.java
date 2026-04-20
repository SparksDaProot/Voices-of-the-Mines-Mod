package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.BloodSplashEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BloodSplashModel extends GeoModel<BloodSplashEntity> {
    @Override
    public ResourceLocation getModelResource(BloodSplashEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/blood_splash.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BloodSplashEntity object) {
        int level = object.getSplashLevel();
        if (level == 1) return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/blood_splash.png");
        if (level == 2) return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/blood_splash_2.png");
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/blood_splash_3.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BloodSplashEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/flesh.animation.json");
    }
}