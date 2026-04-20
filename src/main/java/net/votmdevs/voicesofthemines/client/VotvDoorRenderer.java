package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.VotvDoorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VotvDoorRenderer extends GeoBlockRenderer<VotvDoorBlockEntity> {

    public VotvDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<VotvDoorBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(VotvDoorBlockEntity object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/votv_door.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(VotvDoorBlockEntity object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/votv_door.png");
            }

            @Override
            public ResourceLocation getAnimationResource(VotvDoorBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/votv_door_anim.json");
            }
        });
    }
}