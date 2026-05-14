package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class KavotiaEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 = Idle/Walk, 1 = Attack
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(KavotiaEntity.class, EntityDataSerializers.INT);

    public KavotiaEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.42D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new KavotiaApproachAndAttackGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (entity) -> entity instanceof Player player && player.hasEffect(MobEffects.GLOWING)));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        net.minecraft.sounds.SoundEvent[] walkSounds = {
                VotmSounds.PYRAMID_WALK1.get(),
                VotmSounds.PYRAMID_WALK2.get(),
                VotmSounds.PYRAMID_WALK3.get()
        };
        this.playSound(walkSounds[this.random.nextInt(walkSounds.length)], 0.3F, 1.8F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, event -> {
            int state = this.entityData.get(STATE);
            if (state == 1) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("attack"));

            if (event.isMoving()) return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    static class KavotiaApproachAndAttackGoal extends Goal {
        private final KavotiaEntity kavotia;
        private int attackTicks = 0;
        private boolean isAttacking = false;

        public KavotiaApproachAndAttackGoal(KavotiaEntity kavotia) {
            this.kavotia = kavotia;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = kavotia.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = kavotia.getTarget();
            if (target == null || !target.isAlive()) return false;

            if (isAttacking) {
                return this.attackTicks < 40;
            }
            return true;
        }

        @Override
        public void start() {
            this.attackTicks = 0;
            this.isAttacking = false;
        }

        @Override
        public void tick() {
            LivingEntity target = kavotia.getTarget();
            if (target == null) return;

            kavotia.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (!isAttacking) {
                kavotia.getNavigation().moveTo(target, 1.2D);

                if (kavotia.distanceToSqr(target) < 4.0D) {
                    isAttacking = true;
                    kavotia.entityData.set(STATE, 1);
                    kavotia.getNavigation().stop();
                    attackTicks = 0;
                }
            } else {
                attackTicks++;

                if (attackTicks == 20) {
                    kavotia.playSound(VotmSounds.KAVATTACK.get(), 5.0F, 1.0F);

                    if (kavotia.distanceToSqr(target) < 15.0D) {
                        target.hurt(kavotia.damageSources().mobAttack(kavotia), 8.0F);

                        Vec3 knockbackDir = target.position().subtract(kavotia.position()).normalize();
                        target.setDeltaMovement(target.getDeltaMovement().add(knockbackDir.x * 2.0, 0.5, knockbackDir.z * 2.0));
                        target.hasImpulse = true;
                    }
                }

                if (attackTicks >= 40) {
                    isAttacking = false;
                    kavotia.entityData.set(STATE, 0);
                }
            }
        }

        @Override
        public void stop() {
            this.kavotia.entityData.set(STATE, 0);
            this.isAttacking = false;
            this.attackTicks = 0;
        }
    }
}