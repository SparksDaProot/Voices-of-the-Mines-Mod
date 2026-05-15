package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.item.TapeRecorderItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class TapeRecorderItemRenderer extends GeoItemRenderer<TapeRecorderItem> {
    public TapeRecorderItemRenderer() {
        super(new GeoModel<TapeRecorderItem>() {
            @Override
            public ResourceLocation getModelResource(TapeRecorderItem object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/tape_recorder.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(TapeRecorderItem object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/tape_recorder.png");
            }

            @Override
            public ResourceLocation getAnimationResource(TapeRecorderItem object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/tape_record_animation.json");
            }
        });
    }
}