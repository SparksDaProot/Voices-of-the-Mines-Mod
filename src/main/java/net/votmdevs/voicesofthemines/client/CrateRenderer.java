package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.CrateBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CrateRenderer extends GeoBlockRenderer<CrateBlockEntity> {
    public CrateRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<CrateBlockEntity>() {
            @Override public ResourceLocation getModelResource(CrateBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/crate.geo.json"); }
            @Override public ResourceLocation getTextureResource(CrateBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/crate.png"); }
            @Override public ResourceLocation getAnimationResource(CrateBlockEntity object) { return null; }
        });
    }
}