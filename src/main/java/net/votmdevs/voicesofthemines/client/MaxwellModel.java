package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.MaxwellEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MaxwellModel extends GeoModel<MaxwellEntity> {
    @Override
    public ResourceLocation getModelResource(MaxwellEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/maxwell.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MaxwellEntity object) {
        if (object.isSkeleton()) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/maxwell_skeleton.png");
        }
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/maxwell.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MaxwellEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/maxwell.animation.json");
    }
}