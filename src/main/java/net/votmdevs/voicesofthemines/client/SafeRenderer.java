package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.SafeBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SafeRenderer extends GeoBlockRenderer<SafeBlockEntity> {
    public SafeRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<SafeBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(SafeBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/safe.geo.json");
            }
            @Override
            public ResourceLocation getTextureResource(SafeBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/safe.png");
            }
            @Override
            public ResourceLocation getAnimationResource(SafeBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/safe_animation.json");
            }
        });
    }
}