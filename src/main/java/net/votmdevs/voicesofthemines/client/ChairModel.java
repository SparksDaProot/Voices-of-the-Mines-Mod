package net.votmdevs.voicesofthemines.client;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.block.ChairBlock;
import net.votmdevs.voicesofthemines.block.ChairBlockEntity;
import net.votmdevs.voicesofthemines.entity.SeatEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class ChairModel extends GeoModel<ChairBlockEntity> {

    @Override
    public ResourceLocation getModelResource(ChairBlockEntity object) {
        return new ResourceLocation(
                VoicesOfTheMines.MODID,
                "geo/chair.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(ChairBlockEntity object) {
        return new ResourceLocation(
                VoicesOfTheMines.MODID,
                "textures/block/chair.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(ChairBlockEntity animatable) {
        return new ResourceLocation(
                VoicesOfTheMines.MODID,
                "animations/chair.animation.json"
        );
    }

    @Override
    public void setCustomAnimations(
            ChairBlockEntity animatable,
            long instanceId,
            AnimationState<ChairBlockEntity> animationState
    ) {
        CoreGeoBone upperChair = getAnimationProcessor().getBone("upp");

        if (upperChair == null) {
            return;
        }

        upperChair.setRotY(0.0F);

        if (animatable.getLevel() == null) {
            return;
        }

        AABB seatArea = new AABB(animatable.getBlockPos()).inflate(0.25D);

        for (SeatEntity seat : animatable.getLevel().getEntitiesOfClass(
                SeatEntity.class,
                seatArea
        )) {
            Entity passenger = seat.getFirstPassenger();

            if (!(passenger instanceof Player player)) {
                continue;
            }

            float playerYaw = player.getViewYRot(
                    animationState.getPartialTick()
            );

            Direction placedFacing = animatable
                    .getBlockState()
                    .getValue(ChairBlock.FACING);

            float baseYaw = getFacingYaw(placedFacing);
            float desiredWorldYaw = Mth.wrapDegrees(180.0F - playerYaw);
            float relativeYaw = Mth.wrapDegrees(
                    desiredWorldYaw - baseYaw
            );

            upperChair.setRotY(relativeYaw * Mth.DEG_TO_RAD);
            return;
        }
    }

    private static float getFacingYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 0.0F;
            case WEST -> 90.0F;
            case SOUTH -> 180.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }
}