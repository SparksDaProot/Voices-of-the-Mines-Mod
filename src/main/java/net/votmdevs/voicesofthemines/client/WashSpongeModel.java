package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.WashSpongeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WashSpongeModel extends GeoModel<WashSpongeEntity> {
    @Override
    public ResourceLocation getModelResource(WashSpongeEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/wash_sponge.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WashSpongeEntity object) {
        // texture changing
        if (object.isWet()) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/wet_wash_sponge.png");
        }
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/wash_sponge.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WashSpongeEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/wash_sponge.animation.json");
    }
}