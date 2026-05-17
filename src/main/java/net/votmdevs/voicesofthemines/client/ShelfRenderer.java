package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.ShelfBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ShelfRenderer extends GeoBlockRenderer<ShelfBlockEntity> {
    public ShelfRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<ShelfBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(ShelfBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/shelf.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(ShelfBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/shelf.png"); }
            @Override
            public ResourceLocation getAnimationResource(ShelfBlockEntity object) { return null; }
        });
    }
}