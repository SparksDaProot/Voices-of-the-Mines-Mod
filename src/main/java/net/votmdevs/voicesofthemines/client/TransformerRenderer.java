package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.TransformerBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TransformerRenderer extends GeoBlockRenderer<TransformerBlockEntity> {
    public TransformerRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<TransformerBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(TransformerBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/transformer.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(TransformerBlockEntity animatable) {
                boolean isOn = animatable.isMain ? animatable.isActive : (animatable.mainTransformerPos != null && !animatable.needsReboot && animatable.isNetworkActive);
                return isOn ? new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/transformer.png")
                        : new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/transformer_off.png");
            }

            @Override
            public ResourceLocation getAnimationResource(TransformerBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/transformer_animation.json");
            }
        });

        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/transformer_emissive.png")));
    }
}