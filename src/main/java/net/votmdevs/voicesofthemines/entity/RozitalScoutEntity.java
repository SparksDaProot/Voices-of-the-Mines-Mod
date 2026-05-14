package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkDirection;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RozitalScoutEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 = Idle/Walk, 1 = Attack, 2 = Disappear
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(RozitalScoutEntity.class, EntityDataSerializers.INT);

    public int stateTimer = 0;

    public int sequenceStep = 0;
    public int sequenceTimer = 0;

    public static final Map<UUID, Long> globalIgnoreMap = new HashMap<>();

    public RozitalScoutEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D) // Быстрый шаг
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ScoutApproachAndAttackGoal(this));
        this.goalSelector.addGoal(2, new ScoutObserveGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                this::isNotIgnored));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                (entity) -> isNotIgnored(entity) && (
                        entity.getClass().getSimpleName().contains("WispEntity") ||
                                entity.getClass().getSimpleName().contains("Octahedron")
                )));
    }

    private boolean isNotIgnored(LivingEntity entity) {
        Long ignoredUntil = globalIgnoreMap.get(entity.getUUID());
        if (ignoredUntil == null) return true;
        return entity.level().getGameTime() > ignoredUntil;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            int state = this.entityData.get(STATE);

            if (state == 2) { // Disappear
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                stateTimer++;
                if (stateTimer >= 60) {
                    this.discard();
                }
            } else if (this.tickCount % 40 == 0) {
                List<RozitalPyramidEntity> pyramids = this.level().getEntitiesOfClass(RozitalPyramidEntity.class, this.getBoundingBox().inflate(300.0D));
                if (pyramids.isEmpty()) {
                    this.entityData.set(STATE, 2);
                    this.stateTimer = 0;
                }
            }

            // KAVOTIANS
            if (state == 0 && this.tickCount % 100 == 0) { // Проверяем каждые 5 секунд
                if (this.random.nextInt(4) == 0) { // Шанс 25% (в среднем 1 Кавотия раз в 20 секунд)
                    List<KavotiaEntity> kavotias = this.level().getEntitiesOfClass(KavotiaEntity.class, this.getBoundingBox().inflate(20.0D));

                    if (kavotias.size() < 5 && this.level() instanceof ServerLevel serverLevel) { // Максимум 5 штук рядом
                        KavotiaEntity kavotia = net.votmdevs.voicesofthemines.VoicesOfTheMines.KAVOTIA.get().create(serverLevel);
                        if (kavotia != null) {
                            double ox = this.getX() + (this.random.nextDouble() - 0.5) * 10;
                            double oz = this.getZ() + (this.random.nextDouble() - 0.5) * 10;
                            int groundY = serverLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)ox, (int)oz);

                            kavotia.moveTo(ox, groundY, oz, this.random.nextFloat() * 360, 0);
                            serverLevel.addFreshEntity(kavotia);
                        }
                    }
                }
            }

            if (state == 0 && sequenceStep == 0 && this.random.nextInt(150) == 0) { // Шанс срабатывания
                sequenceStep = 1;
                sequenceTimer = 0;
            }

            if (sequenceStep > 0 && sequenceStep <= 2) {
                sequenceTimer--;
                if (sequenceTimer <= 0) {
                    net.minecraft.sounds.SoundEvent[] scanSounds = {
                            VotmSounds.SCAN1.get(), VotmSounds.SCAN2.get(),
                            VotmSounds.SCAN3.get(), VotmSounds.SCAN4.get()
                    };
                    this.playSound(scanSounds[this.random.nextInt(scanSounds.length)], 1.0F, 1.0F);

                    sequenceStep++;
                    sequenceTimer = 15;
                }
            } else if (sequenceStep > 2) {
                sequenceStep = 0;
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        if (this.entityData.get(STATE) != 0) return; // Только когда ходит

        net.minecraft.sounds.SoundEvent[] walkSounds = {
                VotmSounds.PYRAMID_WALK1.get(),
                VotmSounds.PYRAMID_WALK2.get(),
                VotmSounds.PYRAMID_WALK3.get()
        };
        this.playSound(walkSounds[this.random.nextInt(walkSounds.length)], 4.0F, 1.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }
    @Override
    public boolean isPushable() { return false; }
    @Override
    protected void doPush(net.minecraft.world.entity.Entity entityIn) {}
    @Override
    public boolean canBeCollidedWith() { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, event -> {
            int state = this.entityData.get(STATE);
            if (state == 1) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("attack"));
            if (state == 2) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("disap"));

            if (event.isMoving()) return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    // SPECTATE FOR PLAYER
    static class ScoutObserveGoal extends Goal {
        private final RozitalScoutEntity scout;
        public ScoutObserveGoal(RozitalScoutEntity scout) { this.scout = scout; }

        @Override
        public boolean canUse() {
            LivingEntity target = scout.getTarget();
            return target != null && scout.distanceToSqr(target) >= 100.0D && scout.entityData.get(STATE) == 0;
        }

        @Override
        public void tick() {
            scout.getLookControl().setLookAt(scout.getTarget(), 30, 30);
            if (scout.getRandom().nextInt(30) == 0) {
                double x = scout.getX() + (scout.getRandom().nextDouble() - 0.5) * 15;
                double z = scout.getZ() + (scout.getRandom().nextDouble() - 0.5) * 15;
                scout.getNavigation().moveTo(x, scout.getY(), z, 1.0D);
            }
        }
    }

    // SHOT
    static class ScoutApproachAndAttackGoal extends Goal {
        private final RozitalScoutEntity scout;
        private boolean isAttacking = false;

        public ScoutApproachAndAttackGoal(RozitalScoutEntity scout) {
            this.scout = scout;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = scout.getTarget();
            return target != null && scout.distanceToSqr(target) < 100.0D && scout.entityData.get(STATE) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = scout.getTarget();
            if (target == null || scout.entityData.get(STATE) == 2) return false;

            if (isAttacking) {
                return scout.stateTimer < 80;
            }
            return scout.distanceToSqr(target) < 100.0D;
        }

        @Override
        public void start() {
            this.isAttacking = false;
        }

        @Override
        public void tick() {
            LivingEntity target = scout.getTarget();
            if (target == null) return;

            if (!isAttacking) {
                scout.getLookControl().setLookAt(target, 30.0F, 30.0F);
                scout.getNavigation().moveTo(target, 1.5D);

                if (scout.distanceToSqr(target) <= 2.25D) {
                    isAttacking = true;
                    scout.entityData.set(STATE, 1);
                    scout.stateTimer = 0;
                    scout.getNavigation().stop();
                }
            } else {
                scout.stateTimer++;
                scout.getNavigation().stop();
                scout.getLookControl().setLookAt(target, 30.0F, 30.0F);

                if (scout.stateTimer == 1) {
                    scout.playSound(VotmSounds.SCOUT_SIREN.get(), 3.0F, 1.0F);
                }

                if (target instanceof ServerPlayer sp) {
                    if (scout.stateTimer == 1) {
                        KerfurPacketHandler.INSTANCE.sendTo(new KerfurPacketHandler.ScoutStunPacket(true, false), sp.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    }
                    if (scout.stateTimer == 35) {
                        KerfurPacketHandler.INSTANCE.sendTo(new KerfurPacketHandler.ScoutStunPacket(false, true), sp.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                        scout.playSound(VotmSounds.CAMERA.get(), 6.0F, 1.0F);
                    }
                } else {
                    target.setDeltaMovement(0, 0, 0);
                    if (scout.stateTimer == 35) {
                        scout.playSound(VotmSounds.CAMERA.get(), 2.0F, 1.0F);
                    }
                }

                if (scout.stateTimer >= 80) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 12000, 0, false, false));
                    RozitalScoutEntity.globalIgnoreMap.put(target.getUUID(), scout.level().getGameTime() + 6000);

                    scout.entityData.set(STATE, 0);
                    scout.setTarget(null);
                    isAttacking = false;
                }
            }
        }

        @Override
        public void stop() {
            if (isAttacking && scout.entityData.get(STATE) == 1) {
                scout.entityData.set(STATE, 0);
            }
            isAttacking = false;
            scout.setTarget(null);
        }
    }
}