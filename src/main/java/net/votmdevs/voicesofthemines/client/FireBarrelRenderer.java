package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.FireBarrelBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FireBarrelRenderer extends GeoBlockRenderer<FireBarrelBlockEntity> {
    public FireBarrelRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<FireBarrelBlockEntity>() {
            @Override public ResourceLocation getModelResource(FireBarrelBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/fire_barrel.geo.json"); }
            @Override public ResourceLocation getTextureResource(FireBarrelBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/fire_barrel.png"); }
            @Override public ResourceLocation getAnimationResource(FireBarrelBlockEntity object) { return null; }
        });
    }
}