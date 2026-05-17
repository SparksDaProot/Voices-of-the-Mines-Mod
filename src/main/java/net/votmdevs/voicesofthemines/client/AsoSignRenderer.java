package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.AsoSignBlockEntity;
import net.votmdevs.voicesofthemines.block.DapSignBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class AsoSignRenderer extends GeoBlockRenderer<AsoSignBlockEntity> {
    public AsoSignRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<AsoSignBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(AsoSignBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/aso_sign.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(AsoSignBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/aso_sign.png"); }
            @Override
            public ResourceLocation getAnimationResource(AsoSignBlockEntity object) { return null; }
        });

        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/aso_sign_emissive.png")));
    }
}