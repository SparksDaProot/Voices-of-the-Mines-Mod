package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.ChairBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class ChairModel extends GeoModel<ChairBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ChairBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/chair.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ChairBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/chair.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ChairBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/chair.animation.json");
    }
}