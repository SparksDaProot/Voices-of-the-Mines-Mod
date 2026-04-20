package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.KeypadBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class KeypadRenderer extends GeoBlockRenderer<KeypadBlockEntity> {

    public KeypadRenderer(BlockEntityRendererProvider.Context context) {
        super(new GeoModel<KeypadBlockEntity>() {
            @Override
            public ResourceLocation getModelResource(KeypadBlockEntity object) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "geo/keypad.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(KeypadBlockEntity object) {
                int status = object.getStatus();
                if (status == 0 || status == 2) {
                    return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/keypad_write_code.png");
                }
                else if (status == 3) {
                    return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/keypad_error.png");
                }
                return new ResourceLocation(VoicesOfTheMines.MODID, "textures/block/keypad.png");
            }

            @Override
            public ResourceLocation getAnimationResource(KeypadBlockEntity animatable) {
                return new ResourceLocation(VoicesOfTheMines.MODID, "animations/keypad_buttons.json");
            }
        });

        this.addRenderLayer(new KeypadTextLayer(this));

        this.addRenderLayer(new KeypadEmissiveLayer(this));
    }
}