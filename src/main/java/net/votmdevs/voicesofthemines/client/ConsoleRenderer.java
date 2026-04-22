package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.block.ConsoleBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ConsoleRenderer extends GeoBlockRenderer<ConsoleBlockEntity> {
    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/console_emissive.png");

    public ConsoleRenderer(BlockEntityRendererProvider.Context context) {
        super(new ConsoleModel());
        this.addRenderLayer(new GenericEmissiveLayer<>(this, GLOW_TEXTURE));
    }
}