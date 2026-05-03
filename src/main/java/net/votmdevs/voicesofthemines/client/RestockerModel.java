package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.RestockerEntity;
import software.bernie.geckolib.model.GeoModel;

public class RestockerModel extends GeoModel<RestockerEntity> {
    @Override
    public ResourceLocation getModelResource(RestockerEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/restocker.geo.json"); }
    @Override
    public ResourceLocation getTextureResource(RestockerEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/restocker.png"); }
    @Override
    public ResourceLocation getAnimationResource(RestockerEntity animatable) { return new ResourceLocation(VoicesOfTheMines.MODID, "animations/restocker_animation.json"); }
}