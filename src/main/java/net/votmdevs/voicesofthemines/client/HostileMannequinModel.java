package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.HostileMannequinEntity;
import software.bernie.geckolib.model.GeoModel;

public class HostileMannequinModel extends GeoModel<HostileMannequinEntity> {
    @Override
    public ResourceLocation getModelResource(HostileMannequinEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/mannequin_hostile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HostileMannequinEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/mannequin.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HostileMannequinEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/mannequin_hostile.animation.json");
    }
}