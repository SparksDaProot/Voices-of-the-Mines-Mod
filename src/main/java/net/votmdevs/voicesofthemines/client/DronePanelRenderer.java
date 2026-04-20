package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.block.DronePanelBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DronePanelRenderer extends GeoBlockRenderer<DronePanelBlockEntity> {
    public DronePanelRenderer(BlockEntityRendererProvider.Context context) {
        super(new DronePanelModel());
    }
}