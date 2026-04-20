package net.votmdevs.voicesofthemines.entity;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.votmdevs.voicesofthemines.VotmSounds;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class GarbageEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean wasCollidingLastTick = false;

    public static final EntityDataAccessor<Integer> GARBAGE_LEVEL = SynchedEntityData.defineId(GarbageEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Optional<UUID>> HELD_BY = SynchedEntityData.defineId(GarbageEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public GarbageEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(GARBAGE_LEVEL, 1);
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
                this.playSound(VotmSounds.GARBAGE_DROP.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
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
                if (this.tickCount % 10 == 0 && getGarbageLevel() < 10) {
                    for (GarbageEntity other : this.level().getEntitiesOfClass(GarbageEntity.class, this.getBoundingBox().inflate(1.0D))) {
                        if (other != this && !other.isHeld() && other.isAlive() && this.isAlive()) {
                            int combined = this.getGarbageLevel() + other.getGarbageLevel();
                            if (combined <= 10) {
                                this.setGarbageLevel(combined);
                                other.discard();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, event -> {
            if (isHeld()) {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("hold"));
                return PlayState.CONTINUE;
            } else if (!this.onGround()) {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("hold"));
                return PlayState.CONTINUE;
            } else {
                event.getController().setAnimation(RawAnimation.begin().thenPlay("onground"));
                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GarbageLevel", this.getGarbageLevel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GarbageLevel")) this.setGarbageLevel(tag.getInt("GarbageLevel"));
    }

    public int getGarbageLevel() { return this.entityData.get(GARBAGE_LEVEL); }
    public void setGarbageLevel(int level) { this.entityData.set(GARBAGE_LEVEL, Math.min(10, Math.max(1, level))); }

    public Optional<UUID> getHeldBy() { return this.entityData.get(HELD_BY); }
    public void setHeldBy(@Nullable UUID uuid) { this.entityData.set(HELD_BY, Optional.ofNullable(uuid)); }
    public boolean isHeld() { return getHeldBy().isPresent(); }

    @Override public boolean isPushable() { return false; }
}