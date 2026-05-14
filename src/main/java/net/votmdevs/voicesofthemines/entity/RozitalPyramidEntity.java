package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RozitalPyramidEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0-Asteroid, 1-Appear, 2-Active (Idle/Walk), 3-Attack, 4-Disappear
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(RozitalPyramidEntity.class, EntityDataSerializers.INT);

    public int stateTimer = 0;
    public int killCount = 0;
    public int attackCooldown = 0;
    private final List<LivingEntity> currentTargets = new ArrayList<>();

    public RozitalPyramidEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setMaxUpStep(8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PyramidAttackGoal(this));
        
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                if (this.mob.getRandom().nextInt(20) == 0) {
                    this.forceTrigger = true;
                }
                return super.canUse();
            }
        });

        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                (entity) -> entity instanceof Enemy || entity.getClass().getSimpleName().contains("YellowWispEntity")));
    }

    public boolean isPushable() { return false; }
    @Override
    protected void doPush(net.minecraft.world.entity.Entity entityIn) {}
    @Override
    public boolean canBeCollidedWith() { return false; }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.attackCooldown > 0) this.attackCooldown--;

            int state = this.entityData.get(STATE);

            if (state == 0) {
                this.setDeltaMovement(0, -2.0, 0);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 2, this.getZ(), 5, 0.5, 0.5, 0.5, 0.05);
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2, this.getZ(), 3, 0.3, 0.3, 0.3, 0.02);
                }

                if (this.onGround()) {
                    this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, false, Level.ExplosionInteraction.NONE);
                    this.entityData.set(STATE, 1);
                    this.stateTimer = 0;

                    this.playSound(VotmSounds.TRANSFORM.get(), 5.0F, 1.0F);

                    if (this.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 2; i++) {
                            RozitalScoutEntity scout = net.votmdevs.voicesofthemines.VoicesOfTheMines.ROZITAL_SCOUT.get().create(serverLevel);
                            if (scout != null) {
                                double sx = this.getX() + (this.random.nextDouble() - 0.5) * 8;
                                double sz = this.getZ() + (this.random.nextDouble() - 0.5) * 8;
                                int groundY = serverLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) sx, (int) sz);
                                scout.moveTo(sx, groundY, sz, this.random.nextFloat() * 360, 0);
                                serverLevel.addFreshEntity(scout);

                                // spawn 2 kavotia near scouts
                                for (int j = 0; j < 2; j++) {
                                    KavotiaEntity kavotia = net.votmdevs.voicesofthemines.VoicesOfTheMines.KAVOTIA.get().create(serverLevel);
                                    if (kavotia != null) {
                                        double kx = sx + (this.random.nextDouble() - 0.5) * 6;
                                        double kz = sz + (this.random.nextDouble() - 0.5) * 6;
                                        int kGroundY = serverLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) kx, (int) kz);
                                        kavotia.moveTo(kx, kGroundY, kz, this.random.nextFloat() * 360, 0);
                                        serverLevel.addFreshEntity(kavotia);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else if (state == 1) {
                stateTimer++;
                if (stateTimer >= 80) {
                    this.entityData.set(STATE, 2);
                }
            }
            else if (state == 4) {
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                stateTimer++;
                if (stateTimer >= 80) {
                    this.discard();
                }
            }
        }

        BlockPos pos = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 5, 2))) {
            BlockState bs = this.level().getBlockState(p);
            if (bs.getBlock() instanceof LeavesBlock) {
                this.level().destroyBlock(p, false);
            }
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        if (this.entityData.get(STATE) != 2) return null;
        net.minecraft.sounds.SoundEvent[] ambients = {
                VotmSounds.PIRAMIDPING1.get(),
                VotmSounds.PIRAMIDPING2.get(),
                VotmSounds.PIRAMIDPING3.get()
        };
        return ambients[this.random.nextInt(ambients.length)];
    }

    @Override
    public float getSoundVolume() {
        return 10.0F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        if (this.entityData.get(STATE) != 2) return;

        Player nearestPlayer = this.level().getNearestPlayer(this, 100.0D);
        boolean isFar = nearestPlayer == null || this.distanceToSqr(nearestPlayer) > 400.0D;

        net.minecraft.sounds.SoundEvent[] nearWalks = {
                VotmSounds.PYRAMID_WALK1.get(),
                VotmSounds.PYRAMID_WALK2.get(),
                VotmSounds.PYRAMID_WALK3.get()
        };
        net.minecraft.sounds.SoundEvent[] farWalks = {
                VotmSounds.PYRAMID_FARWALK1.get(),
                VotmSounds.PYRAMID_FARWALK2.get(),
                VotmSounds.PYRAMID_FARWALK3.get()
        };

        net.minecraft.sounds.SoundEvent sound = isFar ? farWalks[this.random.nextInt(farWalks.length)] : nearWalks[this.random.nextInt(nearWalks.length)];
        this.playSound(sound, 15.0F, 1.0F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KillCount", this.killCount);
        tag.putInt("AttackCooldown", this.attackCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.killCount = tag.getInt("KillCount");
        this.attackCooldown = tag.getInt("AttackCooldown");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, event -> {
            int state = this.entityData.get(STATE);
            if (state == 0) return event.setAndContinue(RawAnimation.begin().thenLoop("asteroid"));
            if (state == 1) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("appear"));
            if (state == 3) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("attack"));
            if (state == 4) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("disap"));

            if (event.isMoving()) return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    static class PyramidAttackGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final RozitalPyramidEntity pyramid;
        private int attackTicks = 0;
        private boolean soundPlayed = false;

        public PyramidAttackGoal(RozitalPyramidEntity pyramid) {
            this.pyramid = pyramid;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (pyramid.attackCooldown > 0) return false;

            // KILL COUNT LIMIT = 5 MAY BE CHANGHED
            if (pyramid.killCount >= 5 && pyramid.entityData.get(STATE) == 2) {
                pyramid.entityData.set(STATE, 4);
                pyramid.stateTimer = 0;

                pyramid.playSound(VotmSounds.TRANSFORM.get(), 5.0F, 1.0F);
                return false;
            }
            return pyramid.getTarget() != null && pyramid.entityData.get(STATE) == 2;
        }


        @Override
        public boolean canContinueToUse() {
            return this.attackTicks < 100 && pyramid.isAlive() && pyramid.entityData.get(STATE) == 3;
        }

        @Override
        public void start() {
            this.attackTicks = 0;
            this.soundPlayed = false;
            this.pyramid.entityData.set(STATE, 3);
            this.pyramid.getNavigation().stop();
            this.pyramid.setTarget(null);

            this.pyramid.currentTargets.clear();
            List<LivingEntity> nearby = pyramid.level().getEntitiesOfClass(LivingEntity.class, pyramid.getBoundingBox().inflate(10.0D));
            for (LivingEntity e : nearby) {
                if (e != pyramid && !(e instanceof RozitalPyramidEntity) && (e instanceof Enemy || e.getClass().getSimpleName().contains("YellowWispEntity"))) {
                    if (pyramid.currentTargets.size() < 10) {
                        pyramid.currentTargets.add(e);
                    }
                }
            }
        }

        @Override
        public void tick() {
            attackTicks++;

            if (attackTicks == 20) {
                boolean grabbedAnyone = false;
                for (LivingEntity target : pyramid.currentTargets) {
                    if (target.isAlive()) {
                        grabbedAnyone = true;
                        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, false, false));
                        target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 45, 2, false, false));
                        target.level().playSound(null, target.blockPosition(), VotmSounds.PYRAMID_GRAB.get(), net.minecraft.sounds.SoundSource.HOSTILE, 3.0F, 1.0F);
                    }
                }
                if (grabbedAnyone) {
                    pyramid.playSound(VotmSounds.PYRAMID_HOLD.get(), 6.0F, 1.0F);
                    soundPlayed = true;
                }
            }
            else if (attackTicks > 20 && attackTicks < 45) {
                if (soundPlayed && (attackTicks - 20) % 40 == 0) {
                    pyramid.playSound(VotmSounds.PYRAMID_HOLD.get(), 6.0F, 1.0F);
                }
            }

            // KILL
            if (attackTicks == 45) {
                for (LivingEntity target : pyramid.currentTargets) {
                    if (target.isAlive()) {
                        target.removeEffect(MobEffects.LEVITATION);
                        target.kill();
                        pyramid.killCount++;
                    }
                }
            }
        }

        @Override
        public void stop() {
            if (pyramid.entityData.get(STATE) == 3) {
                pyramid.entityData.set(STATE, 2);
            }
            pyramid.currentTargets.clear();
            pyramid.attackCooldown = 10;
        }
    }
}