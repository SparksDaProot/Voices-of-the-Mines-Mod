package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.model.GeoModel;

public class VotvTerminalModel extends GeoModel<VotvTerminalBlockEntity> {
    @Override
    public ResourceLocation getModelResource(VotvTerminalBlockEntity object) {
        Block block = object.getBlockState().getBlock();
        String name = ForgeRegistries.BLOCKS.getKey(block).getPath();

        if (block == KerfurMod.TERMINAL_CHECK.get() && object.hasDrive()) {
            return new ResourceLocation(KerfurMod.MODID, "geo/terminal_check_drive.geo.json");
        }
        if (block == KerfurMod.TERMINAL_PROCESSING.get() && object.hasDrive()) {
            return new ResourceLocation(KerfurMod.MODID, "geo/terminal_processing_drive.geo.json");
        }
        return new ResourceLocation(KerfurMod.MODID, "geo/" + name + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VotvTerminalBlockEntity object) {
        String name = ForgeRegistries.BLOCKS.getKey(object.getBlockState().getBlock()).getPath();

        if (object.getBlockState().getBlock() == KerfurMod.TERMINAL_CHECK.get() && object.hasDrive()) {
            return new ResourceLocation(KerfurMod.MODID, "textures/block/terminal_check_drive.png");
        }
        if (object.getBlockState().getBlock() == KerfurMod.TERMINAL_PROCESSING.get() && object.hasDrive()) {
            return new ResourceLocation(KerfurMod.MODID, "textures/block/terminal_processing_drive.png");
        }

        return new ResourceLocation(KerfurMod.MODID, "textures/block/" + name + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(VotvTerminalBlockEntity animatable) {
        return null;
    }
}