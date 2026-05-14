package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.RozitalShipEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RozitalShipRenderer extends GeoEntityRenderer<RozitalShipEntity> {
    public RozitalShipRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new ResourceLocation(VoicesOfTheMines.MODID, "rozitalship")));

        // Добавляем Emissive слой
        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/rozitalship_emissive.png")));
    }

    @Override
    public boolean shouldRender(RozitalShipEntity entity, Frustum camera, double camX, double camY, double camZ) {
        // Огромный корабль, рендерим всегда, если он в мире, чтобы избежать пропадания текстур
        return true;
    }
}