package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.RadarBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class RadarRenderer extends GeoBlockRenderer<RadarBlockEntity> {
    public RadarRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<RadarBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(RadarBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/radar.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(RadarBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/radar.png"); }
            @Override
            public ResourceLocation getAnimationResource(RadarBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "animations/empty.animation.json"); }
        });
        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/radar_emissive.png")));
    }
}