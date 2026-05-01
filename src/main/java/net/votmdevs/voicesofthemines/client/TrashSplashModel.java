package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.TrashSplashEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TrashSplashModel extends GeoModel<TrashSplashEntity> {
    @Override
    public ResourceLocation getModelResource(TrashSplashEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/trash_splash.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TrashSplashEntity object) {
        int level = object.getSplashLevel();
        if (level == 1) return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/trash_splash.png");
        if (level == 2) return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/trash_splash_2.png");
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/trash_splash_3.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TrashSplashEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/flesh.animation.json");
    }
}