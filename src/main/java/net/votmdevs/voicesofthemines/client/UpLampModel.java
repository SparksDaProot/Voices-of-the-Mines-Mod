package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.UpLampBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class UpLampModel extends GeoModel<UpLampBlockEntity> {
    @Override
    public ResourceLocation getModelResource(UpLampBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/uplamp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(UpLampBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/uplamp.png");
    }

    @Override
    public ResourceLocation getAnimationResource(UpLampBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/uplamp.animation.json");
    }
}