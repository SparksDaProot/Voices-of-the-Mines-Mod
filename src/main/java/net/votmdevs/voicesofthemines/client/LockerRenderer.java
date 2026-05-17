package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.LockerBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class LockerRenderer extends GeoBlockRenderer<LockerBlockEntity> {
    public LockerRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<LockerBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(LockerBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/locker.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(LockerBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/locker.png"); }
            @Override
            public ResourceLocation getAnimationResource(LockerBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "animations/locker.animation.json"); }
        });
    }
}