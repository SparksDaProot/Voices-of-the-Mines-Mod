package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.GarbageEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GarbageModel extends GeoModel<GarbageEntity> {

    @Override
    public ResourceLocation getModelResource(GarbageEntity object) {
        int level = object.getGarbageLevel();
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/garbage_" + level + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GarbageEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/garbage.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GarbageEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/garbage.animation.json");
    }
}