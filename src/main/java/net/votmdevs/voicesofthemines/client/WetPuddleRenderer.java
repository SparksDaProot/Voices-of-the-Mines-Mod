package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.WetPuddleBlock;
import net.votmdevs.voicesofthemines.block.WetPuddleBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class WetPuddleRenderer extends GeoBlockRenderer<WetPuddleBlockEntity> {
    public WetPuddleRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<WetPuddleBlockEntity>() {
            @Override public ResourceLocation getModelResource(WetPuddleBlockEntity object) { return new ResourceLocation(VoicesOfTheMines.MODID, "geo/wet_puddle.geo.json"); }

            @Override
            public ResourceLocation getTextureResource(WetPuddleBlockEntity object) {
                if (object.getLevel() != null) {
                    net.minecraft.world.level.block.state.BlockState state = object.getLevel().getBlockState(object.getBlockPos());
                    if (state.hasProperty(WetPuddleBlock.DISTANCE)) {
                        int dist = state.getValue(WetPuddleBlock.DISTANCE);
                        if (dist <= 1) return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/wet_puddle.png");
                        if (dist == 2) return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/wet_puddle_middle.png");
                        if (dist == 3) return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/wet_puddle_far.png");
                        if (dist == 4) return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/wet_puddle_veryfar.png");
                    }
                }
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/wet_puddle.png");
            }

            @Override public ResourceLocation getAnimationResource(WetPuddleBlockEntity object) { return null; }
        });
    }

    // Translucent
    @Override
    public RenderType getRenderType(WetPuddleBlockEntity animatable, ResourceLocation texture, net.minecraft.client.renderer.MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}