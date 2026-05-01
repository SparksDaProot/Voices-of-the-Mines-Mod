package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.particles.ParticleTypes;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WashSpongeEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Optional<UUID>> HELD_BY = SynchedEntityData.defineId(WashSpongeEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final EntityDataAccessor<Boolean> IS_WET = SynchedEntityData.defineId(WashSpongeEntity.class, EntityDataSerializers.BOOLEAN);

    private int wetTimer = 0;
    private int soundCooldown = 0;

    public WashSpongeEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0D);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HELD_BY, Optional.empty());
        this.entityData.define(IS_WET, false);
    }

    public Optional<UUID> getHeldBy() { return this.entityData.get(HELD_BY); }
    public void setHeldBy(UUID uuid) { this.entityData.set(HELD_BY, Optional.ofNullable(uuid)); }
    public boolean isHeld() { return getHeldBy().isPresent(); }

    public boolean isWet() { return this.entityData.get(IS_WET); }
    public void setWet(boolean wet) { this.entityData.set(IS_WET, wet); }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {

            if (soundCooldown > 0) {
                soundCooldown--;
            }
            // Wet
            if (this.isInWaterOrBubble()) {
                if (!isWet()) {
                    this.playSound(net.votmdevs.voicesofthemines.VotmSounds.WATER_SPLASH.get(), 1.0F, 1.0F);
                }
                setWet(true);
                wetTimer = 3600; // 3 min
            }

            // dry
            if (isWet()) {
                wetTimer--;
                if (wetTimer <= 0) {
                    setWet(false);
                }
            }

            // grab
            if (isHeld()) {
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

                        // cleaning
                        if (isWet()) {
                            List<TrashSplashEntity> splashes = this.level().getEntitiesOfClass(TrashSplashEntity.class, this.getBoundingBox().inflate(0.5D));
                            for (TrashSplashEntity splash : splashes) {
                                splash.cleanTick();

                                if (this.tickCount % 5 == 0) {
                                    ((ServerLevel) this.level()).sendParticles(ParticleTypes.SPLASH, splash.getX(), splash.getY(), splash.getZ(), 2, 0.2, 0.2, 0.2, 0);
                                }

                                // sounds
                                if (this.soundCooldown <= 0) {
                                    net.minecraft.sounds.SoundEvent[] spongeSounds = {
                                            net.votmdevs.voicesofthemines.VotmSounds.SPONGE1.get(),
                                            net.votmdevs.voicesofthemines.VotmSounds.SPONGE2.get(),
                                            net.votmdevs.voicesofthemines.VotmSounds.SPONGE3.get()
                                    };
                                    net.minecraft.sounds.SoundEvent selectedSponge = spongeSounds[this.random.nextInt(spongeSounds.length)];
                                    this.playSound(selectedSponge, 0.8F, 0.8F + this.random.nextFloat() * 0.4F);

                                    // timer
                                    this.soundCooldown = 20 + this.random.nextInt(11);
                                }
                            }
                        }
                    }
                } else {
                    setHeldBy(null);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsWet", isWet());
        tag.putInt("WetTimer", wetTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setWet(tag.getBoolean("IsWet"));
        wetTimer = tag.getInt("WetTimer");
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
    @Override public boolean isPushable() { return false; }
}