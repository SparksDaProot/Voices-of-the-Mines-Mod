package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.CensorGuyEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CensorGuyRenderer extends GeoEntityRenderer<CensorGuyEntity> {
    public CensorGuyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GeoModel<CensorGuyEntity>() {
            @Override
            public ResourceLocation getModelResource(CensorGuyEntity object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/censorguy.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(CensorGuyEntity object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/censorguy.png");
            }

            @Override
            public ResourceLocation getAnimationResource(CensorGuyEntity object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/censorguy.animation.json");
            }
        });
    }
}