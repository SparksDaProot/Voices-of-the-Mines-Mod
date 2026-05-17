package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.PostalBoxBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PostalBoxRenderer extends GeoBlockRenderer<PostalBoxBlockEntity> {
    public PostalBoxRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<PostalBoxBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(PostalBoxBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/postal_box.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(PostalBoxBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/postal_box.png"); }
            @Override
            public ResourceLocation getAnimationResource(PostalBoxBlockEntity object) { return null; }
        });
    }
}