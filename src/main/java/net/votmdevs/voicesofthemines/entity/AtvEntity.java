package net.votmdevs.voicesofthemines.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AtvEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Boolean> IS_ENGINE_ON = SynchedEntityData.defineId(AtvEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> FUEL = SynchedEntityData.defineId(AtvEntity.class, EntityDataSerializers.FLOAT);

    public static final EntityDataAccessor<java.util.Optional<java.util.UUID>> HELD_BY = SynchedEntityData.defineId(AtvEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final EntityDataAccessor<Float> CURRENT_SPEED = SynchedEntityData.defineId(AtvEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> STEERING = SynchedEntityData.defineId(AtvEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> IS_BRAKING = SynchedEntityData.defineId(AtvEntity.class, EntityDataSerializers.BOOLEAN);

    public AtvEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FUEL, 0.0f);
        this.entityData.define(HELD_BY, java.util.Optional.empty());
        this.entityData.define(IS_ENGINE_ON, false);
        this.entityData.define(CURRENT_SPEED, 0f);
        this.entityData.define(STEERING, 0f);
        this.entityData.define(IS_BRAKING, false);
    }

    public boolean isEngineOn() { return this.entityData.get(IS_ENGINE_ON); }
    public void setEngineOn(boolean on) { this.entityData.set(IS_ENGINE_ON, on); }
    private boolean wasCollidingLastTick = false;
    public void setBraking(boolean braking) {
        boolean wasBraking = this.entityData.get(IS_BRAKING);
        this.entityData.set(IS_BRAKING, braking);

        if (braking && !wasBraking) {
            this.triggerAnim("actions", "brakes_start");
            this.playSound(net.votmdevs.voicesofthemines.KerfurSounds.ATV_BRAKE.get(), 1.0f, 1.0f);
        } else if (!braking && wasBraking) {
            this.triggerAnim("actions", "brakes_end");
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        this.setHealth(this.getHealth() - amount);
        if (this.getHealth() <= 0) {
            this.die(source);
        }
        return true;
    }

    @Override
    public void travel(Vec3 pos) {
        LivingEntity rider = this.getControllingPassenger();

        if (this.isAlive() && this.isVehicle() && rider != null) {
            if (!isEngineOn()) {
                super.travel(Vec3.ZERO);
                return;
            }

            float forward = rider.zza;
            float strafe = rider.xxa;
            boolean braking = this.entityData.get(IS_BRAKING);
            float speed = this.entityData.get(CURRENT_SPEED);

            this.entityData.set(STEERING, strafe);

            if (!braking && forward == 0 && Math.abs(speed) < 0.05f) {
                speed = 0;
            }

            if (braking) {
                speed *= 0.6f;
            } else {
                if (forward > 0) speed += 0.015f;
                else if (forward < 0) speed -= 0.010f;
                else speed *= 0.8f;
            }

            float maxSpeed = 0.8f;
            speed = net.minecraft.util.Mth.clamp(speed, -maxSpeed * 0.4f, maxSpeed);

            if (this.horizontalCollision && Math.abs(speed) > 0.4f) {
                this.triggerAnim("actions", "impact");
                this.hurt(this.damageSources().generic(), 5.0f);

                if (this.level().isClientSide() && rider == net.minecraft.client.Minecraft.getInstance().player) {
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.AtvCrashPacket());
                }

                Vec3 look = this.getLookAngle();
                this.setDeltaMovement(-look.x * 0.5, 0.2, -look.z * 0.5);
                this.hasImpulse = true;

                speed = 0;
            }

            this.entityData.set(CURRENT_SPEED, speed);

            if (Math.abs(speed) > 0.01f && !braking) {
                float turnSpeed = strafe * 4.0f * (speed > 0 ? 1 : -1);
                this.setYRot(this.getYRot() - turnSpeed);
                this.yRotO = this.getYRot();
                this.yBodyRot = this.getYRot();
            }

            this.setSpeed(Math.abs(speed));
            super.travel(new Vec3(0, pos.y, speed > 0 ? 1.0f : (speed < 0 ? -1.0f : 0.0f)));
        } else {
            this.entityData.set(STEERING, 0f);
            this.entityData.set(CURRENT_SPEED, 0f);
            super.travel(pos);
        }
    }

    public java.util.Optional<java.util.UUID> getHeldBy() { return this.entityData.get(HELD_BY); }
    public void setHeldBy(@org.jetbrains.annotations.Nullable java.util.UUID uuid) { this.entityData.set(HELD_BY, java.util.Optional.ofNullable(uuid)); }
    public boolean isHeld() { return getHeldBy().isPresent(); }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && !isHeld()) {
            boolean isColliding = this.horizontalCollision || this.verticalCollision || this.onGround();
            if (isColliding && !this.wasCollidingLastTick) {
                this.triggerAnim("actions", "car_onground");
                this.playSound(net.votmdevs.voicesofthemines.KerfurSounds.GARBAGE_DROP.get(), 1.0F, 1.0F);
            }
            this.wasCollidingLastTick = isColliding;
        }

        if (!this.level().isClientSide() && isEngineOn()) {
            float currentFuel = this.entityData.get(FUEL);
            if (currentFuel > 0) {
                this.entityData.set(FUEL, Math.max(0f, currentFuel - 0.0025f));
            } else {
                this.setEngineOn(false);
                this.playSound(net.votmdevs.voicesofthemines.KerfurSounds.ATV_OFF.get(), 1.0F, 1.0F); // Звук глохнущего мотора
            }
        }

        if (!this.level().isClientSide() && isHeld()) {
            net.minecraft.world.entity.Entity holder = ((net.minecraft.server.level.ServerLevel) this.level()).getEntity(getHeldBy().get());
            if (holder instanceof Player player && player.isAlive()) {
                Vec3 look = player.getLookAngle();
                Vec3 targetPos = player.getEyePosition().add(look.x * 4.0, look.y * 4.0, look.z * 4.0);
                Vec3 diff = targetPos.subtract(this.position());

                if (diff.lengthSqr() > 100.0D) {
                    setHeldBy(null);
                } else {
                    this.setDeltaMovement(diff.scale(0.3D));
                    this.hasImpulse = true;
                    this.fallDistance = 0;
                }
            } else {
                setHeldBy(null);
            }
        }
    }


    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                boolean newState = !isEngineOn();
                if (newState && this.entityData.get(FUEL) <= 0) {
                    this.playSound(net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_CLOSE, 1.0F, 1.0F);
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
                this.setEngineOn(newState);

                if (newState) this.playSound(net.votmdevs.voicesofthemines.KerfurSounds.ATV_ON.get(), 1.0F, 1.0F);
                else this.playSound(net.votmdevs.voicesofthemines.KerfurSounds.ATV_OFF.get(), 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            if (!this.level().isClientSide) player.startRiding(this);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
    }

    @Override
    public void positionRider(net.minecraft.world.entity.Entity passenger, MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
        if (passenger instanceof LivingEntity rider) {
            double offsetY = this.getBbHeight() * 0.25;
            double offsetZ = -0.3;

            float yRot = this.getYRot() * ((float)Math.PI / 180F);
            double rotatedX = -Math.sin(yRot) * offsetZ;
            double rotatedZ = Math.cos(yRot) * offsetZ;

            moveFunction.accept(passenger, this.getX() + rotatedX, this.getY() + offsetY, this.getZ() + rotatedZ);
            rider.yBodyRot = this.yBodyRot;
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.getFirstPassenger() instanceof LivingEntity livingPassenger) {
            return livingPassenger;
        }
        return null;
    }

    @Override public boolean canBeCollidedWith() { return true; }
    @Override public boolean isPushable() { return true; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, event -> {
            if (!isEngineOn()) return event.setAndContinue(RawAnimation.begin().thenLoop("atv_off"));

            boolean braking = this.entityData.get(IS_BRAKING);
            float speed = this.entityData.get(CURRENT_SPEED);
            float steering = this.entityData.get(STEERING);

            if (braking) return event.setAndContinue(RawAnimation.begin().thenLoop("brakes_loop"));

            if (Math.abs(speed) < 0.05f) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("atv_on"));
            }

            if (steering > 0.1f) return event.setAndContinue(RawAnimation.begin().thenLoop("ride_left"));
            if (steering < -0.1f) return event.setAndContinue(RawAnimation.begin().thenLoop("ride_right"));
            if (speed > 0) return event.setAndContinue(RawAnimation.begin().thenLoop("ride"));

            return event.setAndContinue(RawAnimation.begin().thenLoop("ride_back"));
        }));

        registrar.add(new AnimationController<>(this, "actions", 0, event -> PlayState.STOP)
                .triggerableAnim("impact", RawAnimation.begin().thenPlay("impact"))
                .triggerableAnim("car_onground", RawAnimation.begin().thenPlay("car_onground")) // ДОБАВИЛИ СЮДА
                .triggerableAnim("brakes_start", RawAnimation.begin().thenPlay("brakes_start"))
                .triggerableAnim("brakes_end", RawAnimation.begin().thenPlay("brakes_end"))
        );
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("EngineOn", this.isEngineOn());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("EngineOn")) this.setEngineOn(tag.getBoolean("EngineOn"));
    }
}