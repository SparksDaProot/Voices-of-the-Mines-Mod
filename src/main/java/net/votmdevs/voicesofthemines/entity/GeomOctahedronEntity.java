package net.votmdevs.voicesofthemines.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GeomOctahedronEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // states: 0=Idle, 1=Prepare, 2=Charging, 3=Shoot, 4=Close
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(GeomOctahedronEntity.class, EntityDataSerializers.INT);

    public GeomOctahedronEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 30.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new OctahedronAttackGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return VotmSounds.GEOMOCT_LOOP.get();
    }

    @Override
    public void playAmbientSound() {
        if (this.entityData.get(STATE) == 0) {
            super.playAmbientSound();
        }
    }

    @Override
    public float getVoicePitch() {
        return 0.6F + this.random.nextFloat() * 0.8F;
    }



    static class OctahedronAttackGoal extends Goal {
        private final GeomOctahedronEntity octa;
        private int internalTimer = 0;
        private int chargeSoundCooldown = 0;

        public OctahedronAttackGoal(GeomOctahedronEntity octa) {
            this.octa = octa;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return octa.getTarget() != null && octa.getTarget().isAlive() && octa.entityData.get(STATE) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return octa.entityData.get(STATE) == 1;
        }

        @Override
        public void start() {
            internalTimer = 0;
            chargeSoundCooldown = 0;
            octa.getNavigation().stop();
            octa.entityData.set(STATE, 1);
        }

        @Override
        public void tick() {
            LivingEntity target = octa.getTarget();

            // forget about player
            if (target == null || !target.isAlive() || octa.distanceTo(target) > 30.0D) {
                this.stop();
                return;
            }

            octa.getLookControl().setLookAt(target, 30.0F, 30.0F);
            internalTimer++;

            // CHARGING
            if (internalTimer >= 20 && internalTimer < 40) {
                if (chargeSoundCooldown <= 0) {
                    octa.playSound(VotmSounds.OCTCHARGE.get(), 1.0F, 1.0F);

                    int progress = internalTimer - 20;
                    chargeSoundCooldown = Math.max(1, 5 - (progress / 4));
                } else {
                    chargeSoundCooldown--;
                }
            }

            // Shoot
            if (internalTimer == 40) {
                octa.playSound(VotmSounds.OCTSHOOT.get(), 2.0F, 1.0F);

                if (octa.distanceTo(target) <= 15.0D) {
                    target.hurt(octa.damageSources().magic(), 10.0F);
                    target.setSecondsOnFire(5);
                }
            }


            if (internalTimer >= 80) {
                this.stop();
            }
        }

        @Override
        public void stop() {
            octa.entityData.set(STATE, 0);
            internalTimer = 0;

            octa.setTarget(null);
            octa.setLastHurtByMob(null);
            octa.setLastHurtByPlayer(null);
        }
    }

    // Animations

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, event -> {
            int state = this.entityData.get(STATE);

            if (state == 1) {
                return event.setAndContinue(RawAnimation.begin()
                        .thenPlay("prepare")
                        .thenPlay("charging")
                        .thenPlay("shoot")
                        .thenPlay("close")
                        .thenLoop("idle"));
            }
            else if (state == 0) {
                // neutral
                return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }

            return software.bernie.geckolib.core.object.PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}