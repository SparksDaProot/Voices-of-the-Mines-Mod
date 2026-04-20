package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.block.DronePanelBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DronePanelModel extends GeoModel<DronePanelBlockEntity> {
    @Override
    public ResourceLocation getModelResource(DronePanelBlockEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "geo/drone_panel.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DronePanelBlockEntity object) {
        return new ResourceLocation(KerfurMod.MODID, "textures/block/drone_panel.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DronePanelBlockEntity animatable) {
        return new ResourceLocation(KerfurMod.MODID, "animations/drone_panel_animations.json");
    }
}