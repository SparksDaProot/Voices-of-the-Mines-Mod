package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.StreetLampBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class StreetLampRenderer extends GeoBlockRenderer<StreetLampBlockEntity> {
    public StreetLampRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<StreetLampBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(StreetLampBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/street_lamp.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(StreetLampBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/street_lamp.png"); }
            @Override
            public ResourceLocation getAnimationResource(StreetLampBlockEntity object) { return null; }
        });

        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/street_lamp_emissive.png")));
    }
}