package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.votmdevs.voicesofthemines.block.ChairBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ChairRenderer extends GeoBlockRenderer<ChairBlockEntity> {
    public ChairRenderer(BlockEntityRendererProvider.Context context) {
        super(new ChairModel());
    }
}