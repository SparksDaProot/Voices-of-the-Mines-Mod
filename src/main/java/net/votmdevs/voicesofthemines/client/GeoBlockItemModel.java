package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.item.GeoBlockItem;
import software.bernie.geckolib.model.GeoModel;

public class GeoBlockItemModel extends GeoModel<GeoBlockItem> {
    @Override
    public ResourceLocation getModelResource(GeoBlockItem animatable) {
        return animatable.getGeoModel();
    }

    @Override
    public ResourceLocation getTextureResource(GeoBlockItem animatable) {
        return animatable.getGeoTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(GeoBlockItem animatable) {
        return animatable.getGeoAnimation();
    }
}