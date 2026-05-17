package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.DapSignBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DapSignRenderer extends GeoBlockRenderer<DapSignBlockEntity> {
    public DapSignRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<DapSignBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(DapSignBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/dap_sign.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(DapSignBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/dap_sign.png"); }
            @Override
            public ResourceLocation getAnimationResource(DapSignBlockEntity object) { return null; }
        });

        this.addRenderLayer(new net.votmdevs.voicesofthemines.client.GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/dap_sign_emissive.png")));
    }
}