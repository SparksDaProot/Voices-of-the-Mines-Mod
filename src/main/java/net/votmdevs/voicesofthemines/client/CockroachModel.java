package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.entity.CockroachEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CockroachModel extends GeoModel<CockroachEntity> {
    @Override
    public ResourceLocation getModelResource(CockroachEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "geo/cockroach.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CockroachEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "textures/entity/cockroach.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CockroachEntity animatable) {
        return new ResourceLocation(KerfurMod.MODID, "animations/cockroach.animation.json");
    }
}