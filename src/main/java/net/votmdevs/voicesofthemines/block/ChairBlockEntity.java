package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ChairBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private long lastSpinTime = 0;

    public ChairBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.CHAIR_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "spin_controller", 0, state -> {
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }).triggerableAnim("spin", RawAnimation.begin().thenPlay("spin")));
    }
    public void pushSpin() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastSpinTime >= 1050) {
            lastSpinTime = currentTime;
            this.triggerAnim("spin_controller", "spin");
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}