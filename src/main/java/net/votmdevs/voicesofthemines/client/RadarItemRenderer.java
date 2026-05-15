package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.item.RadarItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class RadarItemRenderer extends GeoItemRenderer<RadarItem> {
    public RadarItemRenderer() {
        super(new GeoModel<RadarItem>() {
            @Override
            public ResourceLocation getModelResource(RadarItem object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/radar.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(RadarItem object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/radar.png"); }
            @Override
            public ResourceLocation getAnimationResource(RadarItem object) { return new ResourceLocation(VoicesOfTheMines.MODID, "animations/empty.animation.json"); }
        });
    }
}