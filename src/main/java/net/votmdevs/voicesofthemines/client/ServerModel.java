package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.ServerBlock;
import net.votmdevs.voicesofthemines.block.ServerBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class ServerModel extends GeoModel<ServerBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ServerBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/server.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ServerBlockEntity object) {
        if (object.getBlockState().getValue(ServerBlock.BROKEN)) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/server_broken.png");
        }
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/server.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ServerBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/server.animation.json");
    }
}