package net.votmdevs.voicesofthemines.entity;

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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class FuelCanEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean wasCollidingLastTick = false;
    public static final EntityDataAccessor<Optional<UUID>> HELD_BY = SynchedEntityData.defineId(FuelCanEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final EntityDataAccessor<Float> CAN_FUEL = SynchedEntityData.defineId(FuelCanEntity.class, EntityDataSerializers.FLOAT);

    public FuelCanEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HELD_BY, Optional.empty());
        this.entityData.define(CAN_FUEL, 100.0f);
    }

    public Optional<UUID> getHeldBy() { return this.entityData.get(HELD_BY); }
    public void setHeldBy(UUID uuid) { this.entityData.set(HELD_BY, Optional.ofNullable(uuid)); }
    public boolean isHeld() { return getHeldBy().isPresent(); }

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
                this.playSound(VotmSounds.FUEL_CAN_DROP.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
            this.wasCollidingLastTick = isColliding;
        }

        if (!this.level().isClientSide() && isHeld()) {
            Entity holder = ((ServerLevel) this.level()).getEntity(getHeldBy().get());
            if (holder instanceof Player player && player.isAlive()) {
                Vec3 look = player.getLookAngle();
                Vec3 targetPos = player.getEyePosition().add(look.x * 2.0, look.y * 2.0, look.z * 2.0);
                Vec3 diff = targetPos.subtract(this.position());

                if (diff.lengthSqr() > 64.0D) {
                    setHeldBy(null);
                } else {
                    this.setDeltaMovement(diff.scale(0.3D));
                    this.hasImpulse = true;
                    this.fallDistance = 0;

                    for (AtvEntity atv : this.level().getEntitiesOfClass(AtvEntity.class, this.getBoundingBox().inflate(0.5D))) {
                        float atvFuel = atv.getEntityData().get(AtvEntity.FUEL);
                        float canFuel = this.entityData.get(CAN_FUEL);

                        if (canFuel > 0 && atvFuel < 100.0f) {
                            atv.getEntityData().set(AtvEntity.FUEL, Math.min(100.0f, atvFuel + 0.1f));
                            this.entityData.set(CAN_FUEL, Math.max(0.0f, canFuel - 0.1f));

                            if (this.tickCount % 6 == 0) {
                                this.playSound(VotmSounds.FUEL_POUR.get(), 0.5f, 1.0f);
                            }
                            break;
                        }
                    }
                }
            } else {
                setHeldBy(null);
            }
        }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
    @Override public boolean isPushable() { return false; }
}