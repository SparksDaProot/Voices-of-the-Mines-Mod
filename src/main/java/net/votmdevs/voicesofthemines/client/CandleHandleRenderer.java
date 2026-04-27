package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.CandleHandleBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CandleHandleRenderer extends GeoBlockRenderer<CandleHandleBlockEntity> {
    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/candle_handle_emissive.png");

    public CandleHandleRenderer(BlockEntityRendererProvider.Context context) {
        super(new CandleHandleModel());
        this.addRenderLayer(new GenericEmissiveLayer<>(this, GLOW_TEXTURE));
    }
}