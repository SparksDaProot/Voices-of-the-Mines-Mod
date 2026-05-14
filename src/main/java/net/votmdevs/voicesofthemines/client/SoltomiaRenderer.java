package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.SoltomiaEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SoltomiaRenderer extends GeoEntityRenderer<SoltomiaEntity> {
    public SoltomiaRenderer(EntityRendererProvider.Context renderManager) {
        // ИСПОЛЬЗУЕМ РУЧНУЮ ПРИВЯЗКУ, ЧТОБЫ ИЗБЕЖАТЬ ОШИБОК В ИМЕНАХ ФАЙЛОВ
        super(renderManager, new GeoModel<SoltomiaEntity>() {
            @Override
            public ResourceLocation getModelResource(SoltomiaEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/entity/soltomia.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(SoltomiaEntity animatable) {
                // Указываем точное имя твоей текстуры
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/saltonia.png");
            }

            @Override
            public ResourceLocation getAnimationResource(SoltomiaEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/entity/soltomia.animation.json");
            }
        });

        // Emissive слой
        this.addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/saltonia_emissive.png")));
    }
}