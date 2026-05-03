package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.votmdevs.voicesofthemines.block.VendingBlockEntity;
import net.votmdevs.voicesofthemines.VoicesOfTheMines; // или класс, где будут твои партиклы
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RestockerEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private BlockPos targetVendingPos = null;

    public RestockerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D);
    }

    public void setTargetVending(BlockPos pos) {
        this.targetVendingPos = pos;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.tickCount == 1) {
                spawnSmokeParticles();
            }

            if (this.tickCount >= 40) {
                if (targetVendingPos != null) {
                    BlockEntity be = this.level().getBlockEntity(targetVendingPos);
                    if (be instanceof VendingBlockEntity vending) {
                        vending.refillStock();
                    }
                }

                spawnSmokeParticles();
                this.discard();
            }
        }
    }

    private void spawnSmokeParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            //FOG
            serverLevel.sendParticles(VoicesOfTheMines.BLACK_SMOKE_PARTICLE.get(),
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    30, 0.5D, 1.0D, 0.5D, 0.05D);
        }
    }

    @Override
    public boolean isPushable() { return false; } // Чтобы игрок не мог его сдвинуть

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event ->
                event.setAndContinue(RawAnimation.begin().thenLoop("restock"))
        ));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}