package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.DriveBoxBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class DriveBoxModel extends GeoModel<DriveBoxBlockEntity> {
    @Override
    public ResourceLocation getModelResource(DriveBoxBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/drivebox.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DriveBoxBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/drivebox.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DriveBoxBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/drivebox.animation.json");
    }
}