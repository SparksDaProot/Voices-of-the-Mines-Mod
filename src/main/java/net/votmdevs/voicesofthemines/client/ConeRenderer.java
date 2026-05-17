package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.ConeBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ConeRenderer extends GeoBlockRenderer<ConeBlockEntity> {
    public ConeRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<ConeBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(ConeBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/cone.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(ConeBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/cone.png"); }
            @Override
            public ResourceLocation getAnimationResource(ConeBlockEntity object) { return null; }
        });
    }
}