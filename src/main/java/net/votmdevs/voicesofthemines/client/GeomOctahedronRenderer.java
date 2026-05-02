package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.GeomOctahedronEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GeomOctahedronRenderer extends GeoEntityRenderer<GeomOctahedronEntity> {
    public GeomOctahedronRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GeomOctahedronModel());

        this.addRenderLayer(new GenericEmissiveLayer<>(this,
                new ResourceLocation(VoicesOfTheMines.MODID, "textures/entity/geomoct_emissive.png")));
    }
}