package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.ExtingBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ExtingRenderer extends GeoBlockRenderer<ExtingBlockEntity> {
    public ExtingRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<ExtingBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(ExtingBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/exting.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(ExtingBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/exting.png"); }
            @Override
            public ResourceLocation getAnimationResource(ExtingBlockEntity object) { return null; }
        });
    }
}