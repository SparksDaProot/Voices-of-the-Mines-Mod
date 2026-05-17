package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.CameraWallBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CameraWallRenderer extends GeoBlockRenderer<CameraWallBlockEntity> {
    public CameraWallRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<CameraWallBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(CameraWallBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/camera_wall.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(CameraWallBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/camera_wall.png"); }
            @Override
            public ResourceLocation getAnimationResource(CameraWallBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "animations/camera_wall.animation.json"); }
        });

        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/camera_wall_emissive.png")));
    }
}