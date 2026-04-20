package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FleshEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean wasCollidingLastTick = false;

    public static final EntityDataAccessor<Integer> FLESH_LEVEL = SynchedEntityData.defineId(FleshEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Optional<UUID>> HELD_BY = SynchedEntityData.defineId(FleshEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public FleshEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FLESH_LEVEL, 1);
        this.entityData.define(HELD_BY, Optional.empty());
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            boolean isColliding = this.horizontalCollision || this.verticalCollision;

            if (isColliding && !this.wasCollidingLastTick) {
                // blood
                createOrUpgradeSplash();
                this.playSound(net.votmdevs.voicesofthemines.KerfurSounds.FLESH_DROP.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
            this.wasCollidingLastTick = isColliding;

            if (isHeld()) {
                Entity holder = ((ServerLevel) this.level()).getEntity(getHeldBy().get());
                if (holder instanceof Player player && player.isAlive()) {
                    Vec3 look = player.getLookAngle();
                    Vec3 targetPos = player.getEyePosition().add(look.x * 2.5, look.y * 2.5, look.z * 2.5);
                    Vec3 diff = targetPos.subtract(this.position());

                    if (diff.lengthSqr() > 64.0D) {
                        setHeldBy(null);
                    } else {
                        this.setDeltaMovement(diff.scale(0.3D));
                        this.hasImpulse = true;
                        this.fallDistance = 0;
                    }
                } else {
                    setHeldBy(null);
                }
            } else {
                if (this.tickCount % 10 == 0 && getFleshLevel() < 10) {
                    for (FleshEntity other : this.level().getEntitiesOfClass(FleshEntity.class, this.getBoundingBox().inflate(1.0D))) {
                        if (other != this && !other.isHeld() && other.isAlive() && this.isAlive()) {
                            int combined = this.getFleshLevel() + other.getFleshLevel();
                            if (combined <= 10) {
                                this.setFleshLevel(combined);
                                other.discard();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void createOrUpgradeSplash() {
        Vec3 start = new Vec3(this.xo, this.yo + this.getBbHeight() / 2, this.zo);
        Vec3 delta = this.position().subtract(this.xo, this.yo, this.zo);

        if (delta.lengthSqr() < 0.0001) delta = new Vec3(0, -1, 0);

        Vec3 end = start.add(delta.normalize().scale(1.5D));
        BlockHitResult hit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        Direction face = Direction.UP;
        Vec3 spawnPos = this.position();

        if (hit.getType() == HitResult.Type.BLOCK) {
            face = hit.getDirection();
            spawnPos = hit.getLocation();
        }

        List<BloodSplashEntity> splashes = this.level().getEntitiesOfClass(BloodSplashEntity.class, this.getBoundingBox().inflate(0.3D));

        if (!splashes.isEmpty()) {
            BloodSplashEntity splash = splashes.get(0);
            if (splash.getSplashLevel() < 3) {
                splash.setSplashLevel(splash.getSplashLevel() + 1);
            }
            splash.resetTimer();
        } else {
            BloodSplashEntity newSplash = net.votmdevs.voicesofthemines.KerfurMod.BLOOD_SPLASH.get().create(this.level());
            if (newSplash != null) {
                double offX = spawnPos.x + face.getStepX() * 0.05D;
                double offY = spawnPos.y + face.getStepY() * 0.05D;
                double offZ = spawnPos.z + face.getStepZ() * 0.05D;

                float yaw = 0;
                float pitch = 0;

                switch (face) {
                    case UP -> { pitch = 0; yaw = this.random.nextFloat() * 360F; }
                    case DOWN -> { pitch = 180; yaw = this.random.nextFloat() * 360F; }
                    case NORTH -> { pitch = 90; yaw = 180; }
                    case SOUTH -> { pitch = 90; yaw = 0; }
                    case WEST -> { pitch = 90; yaw = 90; }
                    case EAST -> { pitch = 90; yaw = -90; }
                }

                newSplash.moveTo(offX, offY, offZ, yaw, pitch);

                newSplash.setLockedRotationAndFace(yaw, pitch, face);

                this.level().addFreshEntity(newSplash);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, event -> {
            if (isHeld()) {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("hold"));
            } else if (!this.onGround()) {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("hold"));
            } else {
                event.getController().setAnimation(RawAnimation.begin().thenPlay("onground").thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FleshLevel", this.getFleshLevel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("FleshLevel")) this.setFleshLevel(tag.getInt("FleshLevel"));
    }

    public int getFleshLevel() { return this.entityData.get(FLESH_LEVEL); }
    public void setFleshLevel(int level) { this.entityData.set(FLESH_LEVEL, Math.min(10, Math.max(1, level))); }

    public Optional<UUID> getHeldBy() { return this.entityData.get(HELD_BY); }
    public void setHeldBy(@Nullable UUID uuid) { this.entityData.set(HELD_BY, Optional.ofNullable(uuid)); }
    public boolean isHeld() { return getHeldBy().isPresent(); }

    @Override public boolean isPushable() { return false; }
}