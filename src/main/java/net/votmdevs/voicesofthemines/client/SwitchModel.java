package net.votmdevs.voicesofthemines.client;

import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.SwitchBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class SwitchModel extends GeoModel<SwitchBlockEntity> {
    @Override
    public ResourceLocation getModelResource(SwitchBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/switch.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SwitchBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/switch.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SwitchBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/switch_animation.json");
    }
}