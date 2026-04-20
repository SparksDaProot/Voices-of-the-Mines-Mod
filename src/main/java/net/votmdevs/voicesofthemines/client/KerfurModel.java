package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.entity.KerfurEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KerfurModel extends GeoModel<KerfurEntity> {
    @Override
    public ResourceLocation getModelResource(KerfurEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "geo/kerfur.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KerfurEntity object) {
        String color = object.getKerfurColor();
        if (color.equals("blue") || color.equals("none")) return new ResourceLocation(KerfurMod.MODID, "textures/entity/kerfur.png");
        if (color.equals("abandoned")) return new ResourceLocation(KerfurMod.MODID, "textures/entity/kerfur_abandoned.png");
        return new ResourceLocation(KerfurMod.MODID, "textures/entity/kerfur_" + color + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(KerfurEntity animatable) {
        return new ResourceLocation(KerfurMod.MODID, "animations/kerfur.animation.json");
    }
}