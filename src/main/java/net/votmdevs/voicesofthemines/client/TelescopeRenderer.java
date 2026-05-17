package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.TelescopeBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TelescopeRenderer extends GeoBlockRenderer<TelescopeBlockEntity> {
    public TelescopeRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<TelescopeBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(TelescopeBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/telescope.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(TelescopeBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/telescope.png"); }
            @Override
            public ResourceLocation getAnimationResource(TelescopeBlockEntity object) { return null; }
        });
    }
}