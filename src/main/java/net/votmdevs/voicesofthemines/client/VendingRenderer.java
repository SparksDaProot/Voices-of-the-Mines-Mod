package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.VendingBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VendingRenderer extends GeoBlockRenderer<VendingBlockEntity> {
    public VendingRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<VendingBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(VendingBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/vending.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(VendingBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/vending.png");
            }

            @Override
            public ResourceLocation getAnimationResource(VendingBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/vending_animation.json");
            }
        });

        this.addRenderLayer(new GenericEmissiveLayer<>(this,
                new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/vending_emissive.png")));
    }
}