package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.votmdevs.voicesofthemines.block.ServerBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ServerRenderer extends GeoBlockRenderer<ServerBlockEntity> {
    public ServerRenderer(BlockEntityRendererProvider.Context context) {
        super(new ServerModel());
    }
}