package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.OmegaKerfurEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class OmegaKerfurModel extends GeoModel<OmegaKerfurEntity> {
    @Override
    public ResourceLocation getModelResource(OmegaKerfurEntity object) {
        String accessory = object.getKerfurAccessory();
        String color = object.getKerfurColor();

        if (!accessory.equals("none")) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "geo/omega_kerfur_" + accessory + ".geo.json");
        }

        if (color.equals("blue") || color.equals("none")) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "geo/omega_kerfur.geo.json");
        }
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/omega_kerfur_other.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(OmegaKerfurEntity object) {
        String color = object.getKerfurColor();
        if (color.equals("blue") || color.equals("none")) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/omega_kerfur.png");
        }
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/omega_kerfur_" + color + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(OmegaKerfurEntity object) {
        String accessory = object.getKerfurAccessory();

        if (!accessory.equals("none")) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "animations/omega_kerfur_animations_" + accessory + ".json");
        }

        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/omega_kerfur_animations.json");
    }
}