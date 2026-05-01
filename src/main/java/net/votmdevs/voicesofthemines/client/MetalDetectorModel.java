package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.item.MetalDetectorItem;
import software.bernie.geckolib.model.GeoModel;

public class MetalDetectorModel extends GeoModel<MetalDetectorItem> {

    @Override
    public ResourceLocation getModelResource(MetalDetectorItem object) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "geo/metal_detector.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MetalDetectorItem object) {
        return getTextureBasedOnDistance(false);
    }

    public ResourceLocation getEmissiveResource(MetalDetectorItem object) {
        return getTextureBasedOnDistance(true);
    }

    @Override
    public ResourceLocation getAnimationResource(MetalDetectorItem animatable) {
        return new ResourceLocation(VoicesOfTheMines.MODID, "animations/empty.animation.json");
    }

    // distance
    private ResourceLocation getTextureBasedOnDistance(boolean isEmissive) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return new ResourceLocation(VoicesOfTheMines.MODID, "textures/item/metal_detector" + (isEmissive ? "_emissive" : "") + ".png");
        }

        double closestDist = Double.MAX_VALUE;
        for (Entity e : mc.level.getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.TreasureSpotEntity.class, mc.player.getBoundingBox().inflate(20.0D))) {
            double d = e.distanceToSqr(mc.player);
            if (d < closestDist) {
                closestDist = d;
            }
        }

        String state = "";
        if (closestDist <= 5.0 * 5.0) state = "_here";
        else if (closestDist <= 10.0 * 10.0) state = "_close";
        else if (closestDist <= 20.0 * 20.0) state = "_probably_close";

        String emissiveSuffix = isEmissive ? "_emissive" : "";
        return new ResourceLocation(VoicesOfTheMines.MODID, "textures/item/metal_detector" + state + emissiveSuffix + ".png");
    }
}