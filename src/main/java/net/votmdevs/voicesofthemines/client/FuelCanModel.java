package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.entity.FuelCanEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FuelCanModel extends GeoModel<FuelCanEntity> {
    @Override
    public ResourceLocation getModelResource(FuelCanEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "geo/fuel_can.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FuelCanEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "textures/entity/fuel_can.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FuelCanEntity fuelCan) {
        return null;
    }
}