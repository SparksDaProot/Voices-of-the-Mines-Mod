package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.DroneEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DroneModel extends GeoModel<DroneEntity> {
    @Override
    public ResourceLocation getModelResource(DroneEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/drone.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DroneEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drone.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DroneEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/drone_animation.json");
    }
}