package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.CandleHandleBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class CandleHandleModel extends GeoModel<CandleHandleBlockEntity> {
    @Override
    public ResourceLocation getModelResource(CandleHandleBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/candle_handle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CandleHandleBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/candle_handle.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CandleHandleBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/candle_handle.animation.json");
    }
}