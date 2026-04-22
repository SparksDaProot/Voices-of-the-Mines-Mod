package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.MannequinEntity;
import software.bernie.geckolib.model.GeoModel;

public class MannequinModel extends GeoModel<MannequinEntity> {
    @Override
    public ResourceLocation getModelResource(MannequinEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/mannequin_common.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MannequinEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/mannequin.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MannequinEntity animatable) {
        return null;
    }
}