package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.DronePanelBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DronePanelRenderer extends GeoBlockRenderer<DronePanelBlockEntity> {
    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/drone_panel_emissive.png");

    public DronePanelRenderer(BlockEntityRendererProvider.Context context) {
        super(new DronePanelModel());
        this.addRenderLayer(new GenericEmissiveLayer<>(this, GLOW_TEXTURE));
    }
}