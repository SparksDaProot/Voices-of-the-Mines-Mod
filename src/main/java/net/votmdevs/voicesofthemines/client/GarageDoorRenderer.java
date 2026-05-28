package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.GarageDoorBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GarageDoorRenderer extends GeoBlockRenderer<GarageDoorBlockEntity> {
    public GarageDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<GarageDoorBlockEntity>() {
            @Override public ResourceLocation getModelResource(GarageDoorBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/garage.geo.json"); }
            @Override public ResourceLocation getTextureResource(GarageDoorBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/garage.png"); }
            @Override public ResourceLocation getAnimationResource(GarageDoorBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "animations/garage.animation.json"); }
        });
    }
}