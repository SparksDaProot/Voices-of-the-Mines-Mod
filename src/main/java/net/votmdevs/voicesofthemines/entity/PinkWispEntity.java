package net.votmdevs.voicesofthemines.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PinkWispEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // states
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(PinkWispEntity.class, EntityDataSerializers.INT);

    private int disappearTimer = 0;

    public PinkWispEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D);
    }


    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return VotmSounds.PINK_IDLE.get();
    }

    @Override
    public void playAmbientSound() {
        if (this.entityData.get(STATE) == 0) {
            super.playAmbientSound();
        }
    }

    @Override
    public float getVoicePitch() {
        return 0.9F + this.random.nextFloat() * 0.5F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            int state = this.entityData.get(STATE);

            if (state == 0) {
                Player closestPlayer = this.level().getNearestPlayer(this, 5.0D);

                if (closestPlayer != null && !closestPlayer.isCreative() && !closestPlayer.isSpectator()) {
                    this.entityData.set(STATE, 1);
                    this.getNavigation().stop();
                }
            }
            else {
                disappearTimer++;

                if (state == 1 && disappearTimer >= 40) {
                    this.entityData.set(STATE, 2);
                }

                if (state == 2 && disappearTimer >= 65) {
                    this.discard();
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, event -> {
            int state = this.entityData.get(STATE);

            if (state == 2) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("disapp"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}