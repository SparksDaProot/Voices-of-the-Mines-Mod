package net.votmdevs.voicesofthemines.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractMannequinEntity extends PathfinderMob {
    public static final EntityDataAccessor<Optional<UUID>> HELD_BY = SynchedEntityData.defineId(AbstractMannequinEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected AbstractMannequinEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }



    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HELD_BY, Optional.empty());
    }

    public Optional<UUID> getHeldBy() { return this.entityData.get(HELD_BY); }
    public boolean isHeld() { return getHeldBy().isPresent(); }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSourceIn) {
        return net.votmdevs.voicesofthemines.VotmSounds.MANNEQUIN_HURT.get();
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState blockIn) {
        this.playSound(net.votmdevs.voicesofthemines.VotmSounds.OMEGA_STEP_WOOD.get(), 0.5F, 1.0F);
    }

    public void setHeldBy(@Nullable UUID uuid) {
        boolean wasHeld = this.isHeld();
        this.entityData.set(HELD_BY, Optional.ofNullable(uuid));

        if (wasHeld && uuid == null && !this.level().isClientSide()) {
            this.playSound(net.votmdevs.voicesofthemines.VotmSounds.OMEGA_STEP_WOOD.get(), 1.0F, 1.0F);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && isHeld()) {
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
                    this.setYRot(player.getYRot());
                }
            } else {
                setHeldBy(null);
            }
        }
    }
}