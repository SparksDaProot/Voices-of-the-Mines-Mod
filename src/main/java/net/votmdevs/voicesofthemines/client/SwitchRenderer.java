package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.votmdevs.voicesofthemines.block.SwitchBlockEntity;
import net.votmdevs.voicesofthemines.client.SwitchModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SwitchRenderer extends GeoBlockRenderer<SwitchBlockEntity> {
    public SwitchRenderer(BlockEntityRendererProvider.Context context) {
        super(new SwitchModel());
    }
}