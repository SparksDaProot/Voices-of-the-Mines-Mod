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

public class PlushieBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public PlushieType plushieType;

    public int textTimer = 0;
    public boolean isTextActive = false;

    public PlushieBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.PLUSHIE_BE.get(), pos, state);
        this.plushieType = PlushieType.BENJIKUS;
    }

    public PlushieBlockEntity(BlockPos pos, BlockState state, PlushieType type) {
        super(VoicesOfTheMines.PLUSHIE_BE.get(), pos, state);
        this.plushieType = type;
    }

    public void activateSecretText() {
        this.isTextActive = true;
        this.textTimer = 0;
    }

    public void triggerBeepAnim() {
        triggerAnim("controller", plushieType.getAnimName());
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, PlushieBlockEntity be) {
        if (level.isClientSide && be.isTextActive) {
            be.textTimer++;
            if (be.textTimer > 100) {
                be.isTextActive = false;
                be.textTimer = 0;
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> software.bernie.geckolib.core.object.PlayState.STOP)
                .triggerableAnim(PlushieType.BENJIKUS.getAnimName(), RawAnimation.begin().thenPlay(PlushieType.BENJIKUS.getAnimName())) // beep
                .triggerableAnim(PlushieType.INVINCIBLE.getAnimName(), RawAnimation.begin().thenPlay(PlushieType.INVINCIBLE.getAnimName())) // beepin
        );
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}