package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.DronePanelBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DronePanelModel extends GeoModel<DronePanelBlockEntity> {
    @Override
    public ResourceLocation getModelResource(DronePanelBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/drone_panel.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DronePanelBlockEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/drone_panel.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DronePanelBlockEntity animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/drone_panel_animations.json");
    }
}