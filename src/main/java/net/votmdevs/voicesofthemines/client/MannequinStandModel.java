package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.MannequinStandEntity;
import software.bernie.geckolib.model.GeoModel;

public class MannequinStandModel extends GeoModel<MannequinStandEntity> {
    @Override
    public ResourceLocation getModelResource(MannequinStandEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/stand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MannequinStandEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/mannequin.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MannequinStandEntity animatable) {
        return null;
    }
}