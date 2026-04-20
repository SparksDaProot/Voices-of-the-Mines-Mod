package net.votmdevs.voicesofthemines.entity;

import net.votmdevs.voicesofthemines.VotmSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class DriveEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean wasCollidingLastTick = false;

    public static final EntityDataAccessor<Optional<UUID>> HELD_BY = SynchedEntityData.defineId(DriveEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final EntityDataAccessor<String> SIGNAL_ID = SynchedEntityData.defineId(DriveEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> SIGNAL_TYPE = SynchedEntityData.defineId(DriveEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> SIGNAL_LEVEL = SynchedEntityData.defineId(DriveEntity.class, EntityDataSerializers.INT); // НОВОЕ ПОЛЕ

    public DriveEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HELD_BY, Optional.empty());
        this.entityData.define(SIGNAL_ID, "");
        this.entityData.define(SIGNAL_TYPE, "");
        this.entityData.define(SIGNAL_LEVEL, 0); // Изначально уровень 0 (сырой)
    }

    public Optional<UUID> getHeldBy() { return this.entityData.get(HELD_BY); }
    public void setHeldBy(UUID uuid) { this.entityData.set(HELD_BY, Optional.ofNullable(uuid)); }
    public boolean isHeld() { return getHeldBy().isPresent(); }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            boolean isColliding = this.horizontalCollision || this.verticalCollision;
            if (isColliding && !this.wasCollidingLastTick) {
                SoundEvent[] impactSounds = {VotmSounds.IMPACT_DRIVE_1.get(), VotmSounds.IMPACT_DRIVE_2.get(), VotmSounds.IMPACT_DRIVE_3.get()};
                SoundEvent selectedSound = impactSounds[this.random.nextInt(impactSounds.length)];
                this.playSound(selectedSound, 0.6F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
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

                    net.minecraft.world.phys.AABB box = this.getBoundingBox().inflate(1.0D);
                    net.minecraft.core.BlockPos minPos = net.minecraft.core.BlockPos.containing(box.minX, box.minY, box.minZ);
                    net.minecraft.core.BlockPos maxPos = net.minecraft.core.BlockPos.containing(box.maxX, box.maxY, box.maxZ);

                    boolean inserted = false;
                    for (net.minecraft.core.BlockPos bp : net.minecraft.core.BlockPos.betweenClosed(minPos, maxPos)) {
                        if (inserted) break;
                        net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(bp);

                        if (state.getBlock() == VoicesOfTheMines.TERMINAL_CHECK.get() ||
                                state.getBlock() == VoicesOfTheMines.TERMINAL_PROCESSING.get() ||
                                state.getBlock() == VoicesOfTheMines.PHANTOM_BLOCK.get()) {

                            for (net.minecraft.core.BlockPos searchPos : net.minecraft.core.BlockPos.betweenClosed(bp.offset(-2, -1, -2), bp.offset(2, 1, 2))) {
                                net.minecraft.world.level.block.Block searchBlock = this.level().getBlockState(searchPos).getBlock();

                                if (searchBlock == VoicesOfTheMines.TERMINAL_CHECK.get() ||
                                        searchBlock == VoicesOfTheMines.TERMINAL_PROCESSING.get()) {

                                    net.minecraft.world.level.block.entity.BlockEntity be = this.level().getBlockEntity(searchPos);

                                    if (be instanceof net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal) {
                                        if (!terminal.hasDrive()) {
                                            String sigId = this.entityData.get(SIGNAL_ID);
                                            boolean isEmpty = (sigId == null || sigId.isEmpty());
                                            if (searchBlock == VoicesOfTheMines.TERMINAL_PROCESSING.get() && isEmpty) {
                                                continue;
                                            }
                                            String sigType = this.entityData.get(SIGNAL_TYPE);
                                            int sigLevel = this.entityData.get(SIGNAL_LEVEL);

                                            terminal.setDrive(true, sigId != null ? sigId : "", sigType != null ? sigType : "", sigLevel);

                                            this.level().playSound(null, searchPos, VotmSounds.DRIVE_IN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);

                                            this.discard();
                                            inserted = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                setHeldBy(null);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SignalId", this.entityData.get(SIGNAL_ID));
        tag.putString("SignalType", this.entityData.get(SIGNAL_TYPE));
        tag.putInt("SignalLevel", this.entityData.get(SIGNAL_LEVEL));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SIGNAL_ID, tag.getString("SignalId"));
        this.entityData.set(SIGNAL_TYPE, tag.getString("SignalType"));
        this.entityData.set(SIGNAL_LEVEL, tag.getInt("SignalLevel"));
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
    @Override public boolean isPushable() { return false; }
}