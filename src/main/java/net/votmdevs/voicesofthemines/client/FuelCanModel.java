package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.FuelCanEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FuelCanModel extends GeoModel<FuelCanEntity> {
    @Override
    public ResourceLocation getModelResource(FuelCanEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/fuel_can.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FuelCanEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/fuel_can.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FuelCanEntity fuelCan) {
        return null;
    }
}