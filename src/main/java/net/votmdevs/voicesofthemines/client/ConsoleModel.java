package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.ConsoleBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class ConsoleModel extends GeoModel<ConsoleBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ConsoleBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/console.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ConsoleBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/console.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ConsoleBlockEntity animatable) {
        return null;
    }
}