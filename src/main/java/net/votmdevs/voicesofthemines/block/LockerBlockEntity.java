package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LockerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LockerBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.LOCKER_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            int state = getBlockState().getValue(LockerBlock.STATE);

            if (state == 1) return event.setAndContinue(RawAnimation.begin().thenPlay("open").thenLoop("open_idle"));
            if (state == 2) return event.setAndContinue(RawAnimation.begin().thenPlay("open_ghost").thenLoop("open_idle"));
            if (state == 3) return event.setAndContinue(RawAnimation.begin().thenPlay("close").thenLoop("close_idle"));

            // Если state == 0
            return event.setAndContinue(RawAnimation.begin().thenLoop("close_idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}