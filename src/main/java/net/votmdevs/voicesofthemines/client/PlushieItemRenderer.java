package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.item.PlushieItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PlushieItemRenderer extends GeoItemRenderer<PlushieItem> {
    public PlushieItemRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(VoicesOfTheMines.MODID, "plushie")) {
            @Override
            public ResourceLocation getModelResource(PlushieItem animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/" + animatable.getPlushieType().getModelName() + ".geo.json");
            }
            @Override
            public ResourceLocation getTextureResource(PlushieItem animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/" + animatable.getPlushieType().getTextureName() + ".png");
            }
            @Override
            public ResourceLocation getAnimationResource(PlushieItem animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/" + animatable.getPlushieType().getAnimationFile() + ".json");
            }
        });
    }
}