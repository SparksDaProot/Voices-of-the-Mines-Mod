package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VotvTerminalRenderer extends GeoBlockRenderer<VotvTerminalBlockEntity> {
    public VotvTerminalRenderer(BlockEntityRendererProvider.Context context) {
        super(new VotvTerminalModel());
    }
}