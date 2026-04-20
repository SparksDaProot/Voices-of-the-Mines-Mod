package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.DriveEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DriveModel extends GeoModel<DriveEntity> {
    @Override
    public ResourceLocation getModelResource(DriveEntity object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/drive.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DriveEntity object) {
        String sigId = object.getEntityData().get(DriveEntity.SIGNAL_ID);
        int sigLevel = object.getEntityData().get(DriveEntity.SIGNAL_LEVEL);

        if (sigId == null || sigId.isEmpty()) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_1.png");
        }

        if (sigLevel == 1) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_2_stage_1.png");
        } else if (sigLevel == 2) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_2_stage_2.png");
        } else if (sigLevel >= 3) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_2_stage_3.png");
        }

        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/drive_2.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DriveEntity animatable) { return null; }
}