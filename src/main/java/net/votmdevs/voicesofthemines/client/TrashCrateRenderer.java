package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.TrashCrateBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TrashCrateRenderer extends GeoBlockRenderer<TrashCrateBlockEntity> {
    public TrashCrateRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<TrashCrateBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(TrashCrateBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/trash_crate.geo.json"); }
            @Override
            public ResourceLocation getTextureResource(TrashCrateBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/trash_crate.png"); }
            @Override
            public ResourceLocation getAnimationResource(TrashCrateBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "animations/trash_crate.animation.json"); }
        });
    }
}