package net.votmdevs.voicesofthemines.entity;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;
import java.util.UUID;

public class HookEntity extends Projectile {
    private static final EntityDataAccessor<Boolean> IS_STUCK = SynchedEntityData.defineId(HookEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(HookEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> STUCK_ENTITY_ID = SynchedEntityData.defineId(HookEntity.class, EntityDataSerializers.INT);

    private Vec3 startPos;

    public HookEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public HookEntity(Level level, Player owner) {
        super(VoicesOfTheMines.HOOK_ENTITY.get(), level);
        this.setOwnerUUID(owner.getUUID());
        this.setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
        this.startPos = this.position();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(IS_STUCK, false);
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(STUCK_ENTITY_ID, -1);
    }

    public boolean isStuck() { return this.entityData.get(IS_STUCK); }
    public UUID getOwnerUUID() { return this.entityData.get(OWNER_UUID).orElse(null); }
    public void setOwnerUUID(UUID uuid) { this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid)); }

    public int getStuckEntityId() { return this.entityData.get(STUCK_ENTITY_ID); }

    @Override
    public void tick() {
        super.tick();

        if (this.startPos == null) this.startPos = this.position();

        UUID ownerId = this.getOwnerUUID();
        if (ownerId == null) return;

        Player owner = this.level().getPlayerByUUID(ownerId);
        if (owner == null || owner.getMainHandItem().getItem() != VoicesOfTheMines.HOOK_ITEM.get()) {
            if (!this.level().isClientSide) this.discard();
            return;
        }

        if (!isStuck()) {
            Vec3 pos = this.position();
            Vec3 move = this.getDeltaMovement();
            Vec3 nextPos = pos.add(move);

            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    this.level(), this, pos, nextPos,
                    this.getBoundingBox().expandTowards(move).inflate(1.0D),
                    e -> !e.isSpectator() && e.isAlive() && !e.getUUID().equals(this.getOwnerUUID())
            );

            if (entityHit != null) {
                this.entityData.set(IS_STUCK, true);
                this.entityData.set(STUCK_ENTITY_ID, entityHit.getEntity().getId());
                this.setDeltaMovement(Vec3.ZERO);
                this.playSound(net.minecraft.sounds.SoundEvents.ARROW_HIT_PLAYER, 0.5F, 1.2F);
            } else {
                BlockHitResult blockHit = this.level().clip(new net.minecraft.world.level.ClipContext(pos, nextPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, this));
                if (blockHit.getType() != BlockHitResult.Type.MISS) {
                    this.entityData.set(IS_STUCK, true);
                    this.setDeltaMovement(Vec3.ZERO);
                    this.setPos(blockHit.getLocation().x, blockHit.getLocation().y, blockHit.getLocation().z);
                    this.playSound(net.minecraft.sounds.SoundEvents.ANVIL_PLACE, 0.5F, 2.0F);
                } else {
                    this.setPos(nextPos.x, nextPos.y, nextPos.z);
                }
            }

            if (this.startPos.distanceTo(this.position()) > 25.0D) {
                if (!this.level().isClientSide) this.discard();
            }
        } else {
            int stuckEntityId = this.entityData.get(STUCK_ENTITY_ID);
            if (stuckEntityId != -1) {
                Entity stuckTarget = this.level().getEntity(stuckEntityId);

                if (stuckTarget != null && stuckTarget.isAlive()) {
                    this.setPos(stuckTarget.getX(), stuckTarget.getY() + stuckTarget.getBbHeight() / 2.0, stuckTarget.getZ());
                } else {
                    if (!this.level().isClientSide) this.discard();
                }
            }

            double distanceToOwner = this.distanceTo(owner);
            double MAX_ROPE_LENGTH = 15.0D;
            if (distanceToOwner > MAX_ROPE_LENGTH && !this.level().isClientSide) {
                Vec3 pullVec = this.position().subtract(owner.position()).normalize();
                double pullForce = (distanceToOwner - MAX_ROPE_LENGTH) * 0.15D; // Чем дальше растяжение, тем сильнее тянет
                owner.setDeltaMovement(owner.getDeltaMovement().add(pullVec.scale(pullForce)));
                owner.hurtMarked = true;
                owner.fallDistance = 0.0f;
            }

            if (!owner.onGround() && !this.level().isClientSide && distanceToOwner <= MAX_ROPE_LENGTH) {
                double time = this.level().getGameTime() * 0.1;
                owner.setDeltaMovement(owner.getDeltaMovement().x, Math.sin(time) * 0.05, owner.getDeltaMovement().z);
                owner.hurtMarked = true;
            }
        }
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        UUID ownerId = this.getOwnerUUID();
        if (ownerId != null) tag.putUUID("Owner", ownerId);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) this.setOwnerUUID(tag.getUUID("Owner"));
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}