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

public class TrashCrateBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TrashCrateBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.TRASH_CRATE_BE.get(), pos, state);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            triggerAnim("controller", "interact");
            return true;
        } else if (id == 2) {
            triggerAnim("controller", "push");
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            return event.setAndContinue(RawAnimation.begin());
        })
                .triggerableAnim("interact", RawAnimation.begin().thenPlay("interact"))
                .triggerableAnim("push", RawAnimation.begin().thenPlay("push")));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}