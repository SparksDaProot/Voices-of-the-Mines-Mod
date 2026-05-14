package net.votmdevs.voicesofthemines.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class RozitalShipEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 = appear, 1 = idle, 2 = disappear
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(RozitalShipEntity.class, EntityDataSerializers.INT);

    private int stateTimer = 0;
    private int soundTimer = 0;
    private int aliveTimer = 0;

    public RozitalShipEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(0, 0, 0);
        this.setYRot(this.yRotO);
        this.setXRot(0);

        if (!this.level().isClientSide()) {
            int currentState = this.entityData.get(STATE);
            stateTimer++;
            aliveTimer++;

            if (currentState == 0 && stateTimer > 12) {
                this.entityData.set(STATE, 1);
            }

            // Pyramid kill count check (for end of event)
            if (currentState == 1 && aliveTimer > 200) {
                List<RozitalPyramidEntity> pyramids = this.level().getEntitiesOfClass(RozitalPyramidEntity.class, this.getBoundingBox().inflate(300.0D));

                if (pyramids.isEmpty()) {
                    this.entityData.set(STATE, 2);
                    this.stateTimer = 0;

                    if (this.level() instanceof ServerLevel serverLevel) {
                        for (Player p : serverLevel.players()) {
                            if (p.distanceToSqr(this) < 640000.0D) { // 800 blocks
                                ((net.minecraft.server.level.ServerPlayer) p).connection.send(
                                        new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                                                VotmSounds.ROZITALSHIP_AMBIENCE.getId(),
                                                net.minecraft.sounds.SoundSource.HOSTILE
                                        )
                                );
                            }
                        }
                    }
                }
            }

            if (currentState == 2 && stateTimer > 20) {
                this.discard();
            }

            soundTimer++;
            if (soundTimer >= 200 || soundTimer == 1) {
                soundTimer = 1;
                if (currentState != 2) {
                    this.level().playSound(null, this.blockPosition(), VotmSounds.ROZITALSHIP_AMBIENCE.get(), net.minecraft.sounds.SoundSource.HOSTILE, 5.0F, 1.0F);
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            int state = this.entityData.get(STATE);
            if (state == 0) return event.setAndContinue(RawAnimation.begin().thenPlay("appear"));
            if (state == 2) return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("disappear"));
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entityIn) {}

    @Override
    public boolean canBeCollidedWith() { return false; }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}