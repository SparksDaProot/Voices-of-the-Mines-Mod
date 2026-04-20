package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.entity.DroneEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DroneModel extends GeoModel<DroneEntity> {
    @Override
    public ResourceLocation getModelResource(DroneEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "geo/drone.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DroneEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "textures/entity/drone.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DroneEntity animatable) {
        return new ResourceLocation(KerfurMod.MODID, "animations/drone_animation.json");
    }
}