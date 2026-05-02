package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.GeomOctahedronEntity;
import software.bernie.geckolib.model.GeoModel;

public class GeomOctahedronModel extends GeoModel<GeomOctahedronEntity> {
    @Override
    public ResourceLocation getModelResource(GeomOctahedronEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/geomoct.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeomOctahedronEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/geomoct.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeomOctahedronEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/geomoct_animation.json");
    }
}