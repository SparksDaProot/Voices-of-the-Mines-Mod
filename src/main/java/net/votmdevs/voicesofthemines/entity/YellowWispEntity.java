package net.votmdevs.voicesofthemines.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class YellowWispEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // states
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(YellowWispEntity.class, EntityDataSerializers.INT);

    public YellowWispEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return VotmSounds.YELLOW_IDLE.get();
    }

    @Override
    public void playAmbientSound() {
        if (this.entityData.get(STATE) == 0) {
            super.playAmbientSound();
        }
    }

    @Override
    public float getVoicePitch() {
        return 0.8F + this.random.nextFloat() * 0.4F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new YellowWispAttackGoal(this)); // Наш кастомный ИИ
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    static class YellowWispAttackGoal extends Goal {
        private final YellowWispEntity wisp;
        private Player targetPlayer = null;
        private int attackTimer = 0;

        public YellowWispAttackGoal(YellowWispEntity wisp) {
            this.wisp = wisp;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (wisp.entityData.get(STATE) != 0) return false;

            Player closest = wisp.level().getNearestPlayer(wisp, 10.0D);
            if (closest != null && closest.isAlive() && !closest.isCreative() && !closest.isSpectator()) {
                this.targetPlayer = closest;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return targetPlayer != null && targetPlayer.isAlive() && !targetPlayer.isCreative() && !targetPlayer.isSpectator();
        }

        @Override
        public void start() {
            attackTimer = 0;
            wisp.entityData.set(STATE, 1);
        }

        @Override
        public void tick() {
            if (targetPlayer == null || !targetPlayer.isAlive()) {
                this.stop();
                return;
            }

            double dist = wisp.distanceTo(targetPlayer);

            if (wisp.entityData.get(STATE) == 1) {
                // ПОГОНЯ
                wisp.getNavigation().moveTo(targetPlayer, 1.2D);
                wisp.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);

                if (dist < 2.5D) {
                    wisp.entityData.set(STATE, 2);
                    wisp.getNavigation().stop();
                }
            }
            else if (wisp.entityData.get(STATE) == 2) {
                wisp.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);

                if (dist > 4.0D) {
                    wisp.entityData.set(STATE, 1);
                    return;
                }

                net.minecraft.world.phys.Vec3 lookVec = wisp.getLookAngle();

                double targetX = wisp.getX() + lookVec.x * 1.5D;
                double targetY = wisp.getY() + 2.0D;
                double targetZ = wisp.getZ() + lookVec.z * 1.5D;

                double dx = targetX - targetPlayer.getX();
                double dy = targetY - targetPlayer.getY();
                double dz = targetZ - targetPlayer.getZ();

                double bobbing = Math.sin(wisp.tickCount * 0.1) * 0.05;

                targetPlayer.setDeltaMovement(dx * 0.15, (dy * 0.1) + bobbing, dz * 0.15);
                targetPlayer.hurtMarked = true;

                targetPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 9, false, false, false));

                attackTimer++;
                if (attackTimer >= 20) {
                    targetPlayer.hurt(wisp.damageSources().magic(), 5.0F);

                    int fleshCount = 3 + wisp.random.nextInt(3);
                    for (int i = 0; i < fleshCount; i++) {
                        Entity flesh = VoicesOfTheMines.FLESH.get().create(wisp.level());
                        if (flesh != null) {
                            flesh.setPos(targetPlayer.getX(), targetPlayer.getY() - 0.5D, targetPlayer.getZ());
                            double fdx = (wisp.random.nextDouble() - 0.5) * 0.4;
                            double fdy = wisp.random.nextDouble() * 0.3;
                            double fdz = (wisp.random.nextDouble() - 0.5) * 0.4;
                            flesh.setDeltaMovement(fdx, fdy, fdz);
                            wisp.level().addFreshEntity(flesh);
                        }
                    }
                    attackTimer = 0;
                }
            }
        }

        @Override
        public void stop() {
            wisp.entityData.set(STATE, 0);
            targetPlayer = null;
            attackTimer = 0;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event ->
                event.setAndContinue(RawAnimation.begin().thenLoop("idle"))
        ));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}