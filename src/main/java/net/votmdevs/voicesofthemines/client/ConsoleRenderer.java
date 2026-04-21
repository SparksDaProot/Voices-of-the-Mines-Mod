package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.votmdevs.voicesofthemines.block.ConsoleBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ConsoleRenderer extends GeoBlockRenderer<ConsoleBlockEntity> {
    public ConsoleRenderer(BlockEntityRendererProvider.Context context) {
        super(new ConsoleModel());
    }
}