package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.entity.FleshEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FleshModel extends GeoModel<FleshEntity> {

    @Override
    public ResourceLocation getModelResource(FleshEntity object) {
        int level = object.getFleshLevel();
        if (level <= 1) {
            return new ResourceLocation(KerfurMod.MODID, "geo/flesh.geo.json");
        }
        return new ResourceLocation(KerfurMod.MODID, "geo/flesh_" + level + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FleshEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "textures/entity/flesh.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FleshEntity animatable) {
        return new ResourceLocation(KerfurMod.MODID, "animations/flesh.animation.json");
    }
}