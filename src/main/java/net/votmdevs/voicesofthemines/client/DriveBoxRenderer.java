package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.DriveBoxBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DriveBoxRenderer extends GeoBlockRenderer<DriveBoxBlockEntity> {

    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/drivebox_emissive.png");

    public DriveBoxRenderer(BlockEntityRendererProvider.Context context) {
        super(new DriveBoxModel());

        this.addRenderLayer(new GenericEmissiveLayer<>(this, GLOW_TEXTURE));
    }
}