package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.RadioBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class RadioRenderer extends GeoBlockRenderer<RadioBlockEntity> {
    public RadioRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<RadioBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(RadioBlockEntity animatable) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/radio.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(RadioBlockEntity animatable) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/radio.png"); }
            @Override
            public ResourceLocation getAnimationResource(RadioBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/empty.animation.json");
            }
        });

        this.addRenderLayer(new GenericEmissiveLayer<>(this,
                new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/radio_emissive.png")));
    }
}