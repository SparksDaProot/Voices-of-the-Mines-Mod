package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class SoltomiaEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 = Idle/Walk, 2 = Laugh, 3 = Wash, 4 = Disappear
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(SoltomiaEntity.class, EntityDataSerializers.INT);

    public int actionTimer = 0;

    public int sequenceStep = 0;
    public int sequenceTimer = 0;

    public SoltomiaEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SoltomiaBehaviorGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            int state = this.entityData.get(STATE);

            if (state == 2) { // Laugh
                actionTimer++;
                if (actionTimer >= 40) {
                    this.entityData.set(STATE, 0);
                    actionTimer = 0;
                }
            }
            else if (state == 3) { // Wash
                actionTimer++;

                if (actionTimer == 10 || actionTimer == 35 || actionTimer == 60) {
                    net.minecraft.sounds.SoundEvent[] spongeSounds = {
                            VotmSounds.SPONGE1.get(),
                            VotmSounds.SPONGE2.get(),
                            VotmSounds.SPONGE3.get()
                    };
                    this.playSound(spongeSounds[this.random.nextInt(spongeSounds.length)], 1.0F, 1.0F);
                }

                // wash particles
                if (actionTimer % 2 == 0 && this.level() instanceof ServerLevel serverLevel) {
                    Player p = this.level().getNearestPlayer(this, 5.0D);
                    if (p != null) {
                        for (int i = 0; i < 20; i++) {
                            double px = p.getX() + (this.random.nextDouble() - 0.5) * 2.0;
                            double py = p.getY() + this.random.nextDouble() * 2.5;
                            double pz = p.getZ() + (this.random.nextDouble() - 0.5) * 2.0;

                            serverLevel.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, px, py, pz, 3, 0.2, 0.2, 0.2, 0.05);
                            serverLevel.sendParticles(ParticleTypes.SPIT, px, py, pz, 2, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }

                if (actionTimer >= 80) {
                    this.entityData.set(STATE, 0);
                    actionTimer = 0;
                }
            }
            else if (state == 4) { // Disappear
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                actionTimer++;
                if (actionTimer >= 40) {
                    this.discard();
                }
            }

            // ЛОГИКА ТРОЙНОГО ЗВУКА
            if (state == 0 && sequenceStep == 0 && this.random.nextInt(300) == 0) {
                sequenceStep = 1;
                sequenceTimer = 0;
            }

            if (sequenceStep > 0 && sequenceStep <= 3) {
                sequenceTimer--;
                if (sequenceTimer <= 0) {
                    net.minecraft.sounds.SoundEvent[] vSounds = {
                            VotmSounds.SOLTV1.get(), VotmSounds.SOLTV2.get(), VotmSounds.SOLTV3.get(),
                            VotmSounds.SOLTV4.get(), VotmSounds.SOLTV5.get(), VotmSounds.SOLTV6.get(),
                            VotmSounds.SOLTV7.get(), VotmSounds.SOLTV8.get(), VotmSounds.SOLTV9.get()
                    };
                    this.playSound(vSounds[this.random.nextInt(vSounds.length)], 1.0F, 1.0F);

                    sequenceStep++;
                    sequenceTimer = 15;
                }
            } else if (sequenceStep > 3) {
                sequenceStep = 0;
            }

            if (state != 4 && this.tickCount % 40 == 0) {
                List<RozitalPyramidEntity> pyramids = this.level().getEntitiesOfClass(RozitalPyramidEntity.class, this.getBoundingBox().inflate(300.0D));
                if (pyramids.isEmpty()) {
                    this.entityData.set(STATE, 4);
                    this.actionTimer = 0;
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, event -> {
            int state = this.entityData.get(STATE);

            if (state == 2) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("laugh"));
            if (state == 3) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("wash"));
            if (state == 4) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("disap"));

            if (event.isMoving()) return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    // laugh / wash
    static class SoltomiaBehaviorGoal extends Goal {
        private final SoltomiaEntity soltomia;
        private Player targetPlayer;
        private int standStillTimer = 0;
        private Vec3 lastPlayerPos = Vec3.ZERO;

        public SoltomiaBehaviorGoal(SoltomiaEntity soltomia) {
            this.soltomia = soltomia;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (soltomia.entityData.get(STATE) == 4) return false;

            this.targetPlayer = soltomia.level().getNearestPlayer(soltomia, 15.0D);
            return this.targetPlayer != null && !this.targetPlayer.isSpectator();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            this.standStillTimer = 0;
            if (this.targetPlayer != null) {
                this.lastPlayerPos = this.targetPlayer.position();
            }
        }

        @Override
        public void tick() {
            if (targetPlayer == null) return;

            int currentState = soltomia.entityData.get(STATE);

            // ЕСЛИ МОЕТ ИЛИ СМЕЕТСЯ - ПРОСТО СМОТРИТ НА ИГРОКА И СТОИТ
            if (currentState == 2 || currentState == 3) {
                soltomia.getNavigation().stop();
                soltomia.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);
                return;
            }

            soltomia.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);
            double distSq = soltomia.distanceToSqr(targetPlayer);

            if (distSq > 4.0D) {
                soltomia.getNavigation().moveTo(targetPlayer, 1.0D);
            } else if (distSq < 2.0D) {
                Vec3 away = soltomia.position().subtract(targetPlayer.position()).normalize().scale(1.5);
                soltomia.getNavigation().moveTo(soltomia.getX() + away.x, soltomia.getY(), soltomia.getZ() + away.z, 1.0D);
            } else {
                soltomia.getNavigation().stop();
            }

            Vec3 currentPos = targetPlayer.position();
            if (currentPos.distanceToSqr(lastPlayerPos) < 0.01D) {
                standStillTimer++;
            } else {
                standStillTimer = 0;
            }
            lastPlayerPos = currentPos;

            if (standStillTimer >= 200 && distSq <= 9.0D) {
                soltomia.entityData.set(STATE, 3);
                soltomia.actionTimer = 0;
                standStillTimer = 0;
                soltomia.getNavigation().stop();
            }
            else if (standStillTimer > 40 && soltomia.getRandom().nextInt(150) == 0 && distSq <= 9.0D) {
                soltomia.entityData.set(STATE, 2);
                soltomia.actionTimer = 0;
                soltomia.getNavigation().stop();

                net.minecraft.sounds.SoundEvent laughSound;
                if (soltomia.getRandom().nextInt(50) == 0) { // Шанс 1 к 50 (2%)
                    laughSound = VotmSounds.SOLTLAUGH3.get();
                } else {
                    laughSound = soltomia.getRandom().nextBoolean() ? VotmSounds.SOLTLAUGH1.get() : VotmSounds.SOLTLAUGH2.get();
                }
                soltomia.playSound(laughSound, 0.5F, 1.0F);
            }
        }
    }
}