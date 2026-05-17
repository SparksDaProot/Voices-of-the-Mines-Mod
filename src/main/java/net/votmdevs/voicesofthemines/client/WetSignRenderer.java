package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.WetSignBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class WetSignRenderer extends GeoBlockRenderer<WetSignBlockEntity> {
    public WetSignRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<WetSignBlockEntity>() {
            @Override public ResourceLocation getModelResource(WetSignBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/wet_sign.geo.json"); }
            @Override public ResourceLocation getTextureResource(WetSignBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/wet_sign.png"); }
            @Override public ResourceLocation getAnimationResource(WetSignBlockEntity object) { return null; }
        });
    }
}