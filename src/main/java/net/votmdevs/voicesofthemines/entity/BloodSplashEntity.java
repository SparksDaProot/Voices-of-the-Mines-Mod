package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BloodSplashEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Integer> SPLASH_LEVEL = SynchedEntityData.defineId(BloodSplashEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> LOCKED_PITCH = SynchedEntityData.defineId(BloodSplashEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> LOCKED_YAW = SynchedEntityData.defineId(BloodSplashEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Byte> ATTACHED_FACE = SynchedEntityData.defineId(BloodSplashEntity.class, EntityDataSerializers.BYTE);

    private int lifeTicks = 0;
    private static final int MAX_LIFE = 600;

    public BloodSplashEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPLASH_LEVEL, 1);
        this.entityData.define(LOCKED_PITCH, 0.0F);
        this.entityData.define(LOCKED_YAW, 0.0F);
        this.entityData.define(ATTACHED_FACE, (byte) Direction.UP.get3DDataValue());
    }

    @Override
    public void tick() {
        this.setDeltaMovement(0, 0, 0);

        float pitch = this.entityData.get(LOCKED_PITCH);
        float yaw = this.entityData.get(LOCKED_YAW);

        this.setXRot(pitch);
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        this.xRotO = pitch;
        this.yRotO = yaw;
        this.yBodyRotO = yaw;
        this.yHeadRotO = yaw;

        super.tick();

        if (!this.level().isClientSide()) {
            lifeTicks++;
            if (lifeTicks >= MAX_LIFE) {
                this.discard();
                return;
            }

            Direction face = Direction.from3DDataValue(this.entityData.get(ATTACHED_FACE));

            BlockPos attachedPos = BlockPos.containing(
                    this.getX() - face.getStepX() * 0.1,
                    this.getY() - face.getStepY() * 0.1,
                    this.getZ() - face.getStepZ() * 0.1
            );

            if (!this.level().getBlockState(attachedPos).isSolidRender(this.level(), attachedPos)) {
                this.discard();
            }
        }
    }

    public void resetTimer() {
        this.lifeTicks = 0;
    }

    public void setLockedRotationAndFace(float yaw, float pitch, Direction face) {
        this.entityData.set(LOCKED_YAW, yaw);
        this.entityData.set(LOCKED_PITCH, pitch);
        this.entityData.set(ATTACHED_FACE, (byte) face.get3DDataValue());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SplashLevel", this.getSplashLevel());
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putFloat("LockedPitch", this.entityData.get(LOCKED_PITCH));
        tag.putFloat("LockedYaw", this.entityData.get(LOCKED_YAW));
        tag.putByte("AttachedFace", this.entityData.get(ATTACHED_FACE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SplashLevel")) this.setSplashLevel(tag.getInt("SplashLevel"));
        if (tag.contains("LifeTicks")) this.lifeTicks = tag.getInt("LifeTicks");
        if (tag.contains("LockedPitch")) this.entityData.set(LOCKED_PITCH, tag.getFloat("LockedPitch"));
        if (tag.contains("LockedYaw")) this.entityData.set(LOCKED_YAW, tag.getFloat("LockedYaw"));
        if (tag.contains("AttachedFace")) this.entityData.set(ATTACHED_FACE, tag.getByte("AttachedFace"));
    }

    public int getSplashLevel() { return this.entityData.get(SPLASH_LEVEL); }
    public void setSplashLevel(int level) { this.entityData.set(SPLASH_LEVEL, Math.min(3, Math.max(1, level))); }

    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entityIn) { }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isInvulnerable() { return true; }
}