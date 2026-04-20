package net.votmdevs.voicesofthemines.entity;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.KerfurSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

public class MaxwellEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Optional<UUID>> HELD_BY = SynchedEntityData.defineId(MaxwellEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final EntityDataAccessor<Boolean> DANCING = SynchedEntityData.defineId(MaxwellEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> SKELETON = SynchedEntityData.defineId(MaxwellEntity.class, EntityDataSerializers.BOOLEAN);

    private int petCombo = 0;
    private long lastPetTime = 0;
    private int skeletonTicks = 0;
    private int themeTimer = 0;

    public boolean musicStopped = false;
    private boolean wasDancing = false;
    private boolean wasHeld = false;
    private int scrapeTimer = 0;
    private boolean wasScraping = false;

    public MaxwellEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HELD_BY, Optional.empty());
        this.entityData.define(DANCING, false);
        this.entityData.define(SKELETON, false);
    }

    private void stopCustomSound(net.minecraft.sounds.SoundEvent sound) {
        if (!this.level().isClientSide()) {
            for (Player p : this.level().players()) {
                if (p instanceof ServerPlayer sp && this.distanceToSqr(p) < 1024) {
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                            sound.getLocation(),
                            net.minecraft.sounds.SoundSource.NEUTRAL
                    ));
                }
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (isSkeleton()) {
                skeletonTicks++;
                if (skeletonTicks == 1) {
                    this.playSound(KerfurSounds.DUDUDU.get(), 1.0F, 1.0F);
                }
                if (skeletonTicks >= 30) {
                    this.level().explode(this, this.getX(), this.getY(), this.getZ(), 3.0F, Level.ExplosionInteraction.MOB);
                    this.discard();
                }
                return;
            }

            checkNearbyJukebox();
            boolean currentlyDancing = isDancing();
            boolean currentlyHeld = isHeld();

            if ((currentlyDancing && !wasDancing) || (currentlyHeld && !wasHeld)) {
                stopCustomSound(KerfurSounds.MAXWELL_THEME.get());
                stopCustomSound(KerfurSounds.CONCRETE_SCRAPE.get());
                wasScraping = false; // Сбрасываем скрежет при поднятии
            }

            if (!currentlyDancing && !musicStopped && !currentlyHeld) {
                themeTimer--;
                if (themeTimer <= 0) {
                    stopCustomSound(KerfurSounds.MAXWELL_THEME.get());
                    this.playSound(KerfurSounds.MAXWELL_THEME.get(), 0.3F, 1.0F);
                    themeTimer = 1380;
                }
            } else if (currentlyDancing || currentlyHeld) {
                themeTimer = 10;
            }

            wasDancing = currentlyDancing;
            wasHeld = currentlyHeld;

            if (currentlyHeld) {
                Entity holder = ((ServerLevel) this.level()).getEntity(getHeldBy().get());
                if (holder instanceof Player player && player.isAlive()) {
                    Vec3 look = player.getLookAngle();
                    Vec3 targetPos = player.getEyePosition().add(look.x * 2.5, look.y * 2.5, look.z * 2.5);
                    Vec3 diff = targetPos.subtract(this.position());

                    if (diff.lengthSqr() > 64.0D) {
                        setHeldBy(null);
                        if (wasScraping) {
                            stopCustomSound(KerfurSounds.CONCRETE_SCRAPE.get());
                            wasScraping = false;
                        }
                    } else {
                        this.setDeltaMovement(diff.scale(0.3D));
                        this.hasImpulse = true;
                        this.fallDistance = 0;
                        this.setYRot(player.getYRot());

                        boolean isMoving = diff.lengthSqr() > 0.05D;
                        boolean currentlyScraping = isMoving && (this.onGround() || this.horizontalCollision);

                        if (currentlyScraping) {
                            if (!wasScraping) {
                                this.playSound(KerfurSounds.CONCRETE_SCRAPE.get(), 0.3F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.1F);
                                scrapeTimer = 80;
                            } else {
                                scrapeTimer--;
                                if (scrapeTimer <= 0) {
                                    stopCustomSound(KerfurSounds.CONCRETE_SCRAPE.get());
                                    this.playSound(KerfurSounds.CONCRETE_SCRAPE.get(), 0.3F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.1F);
                                    scrapeTimer = 80;
                                }
                            }
                        } else {
                            if (wasScraping) {
                                stopCustomSound(KerfurSounds.CONCRETE_SCRAPE.get());
                            }
                            scrapeTimer = 0;
                        }

                        wasScraping = currentlyScraping;
                    }
                } else {
                    setHeldBy(null);
                    if (wasScraping) {
                        stopCustomSound(KerfurSounds.CONCRETE_SCRAPE.get());
                        wasScraping = false;
                    }
                }
            } else {
                if (wasScraping) {
                    stopCustomSound(KerfurSounds.CONCRETE_SCRAPE.get());
                    wasScraping = false;
                }
            }
        }
    }

    private void checkNearbyJukebox() {
        BlockPos pos = this.blockPosition();
        boolean musicFound = false;
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-5, -1, -5), pos.offset(5, 1, 5))) {
            BlockState state = this.level().getBlockState(nearby);
            if (state.is(Blocks.JUKEBOX) && state.hasProperty(JukeboxBlock.HAS_RECORD) && state.getValue(JukeboxBlock.HAS_RECORD)) {
                musicFound = true;
                break;
            }
        }
        this.entityData.set(DANCING, musicFound);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || isSkeleton()) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide()) {
                if (!musicStopped) {
                    musicStopped = true;
                    stopCustomSound(KerfurSounds.MAXWELL_THEME.get());
                }

                long time = this.level().getGameTime();
                if (time - lastPetTime < 15) petCombo++;
                else petCombo = 1;
                lastPetTime = time;

                if (petCombo >= 15) {
                    this.setSkeleton(true);
                } else {
                    float pitch = 1.0F + (petCombo * 0.1F);
                    this.playSound(KerfurSounds.MEOW_1.get(), 1.0F, pitch);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isSkeleton()) return false;

        if (source.getEntity() instanceof Player && !this.level().isClientSide()) {
            stopCustomSound(KerfurSounds.MAXWELL_THEME.get());
            stopCustomSound(KerfurSounds.CONCRETE_SCRAPE.get());

            this.playSound(this.random.nextBoolean() ? KerfurSounds.MEOW_1.get() : KerfurSounds.MEOW_2.get(), 1.0f, 1.0f);
            this.spawnAtLocation(KerfurMod.MAXWELL_ITEM.get());
            this.discard();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, event -> {
            if (isSkeleton()) return PlayState.STOP;
            if (isDancing()) return event.setAndContinue(RawAnimation.begin().thenLoop("dance"));
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle_maxwell"));
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
    @Override public boolean isPushable() { return false; }

    public Optional<UUID> getHeldBy() { return this.entityData.get(HELD_BY); }
    public void setHeldBy(@Nullable UUID uuid) { this.entityData.set(HELD_BY, Optional.ofNullable(uuid)); }
    public boolean isHeld() { return getHeldBy().isPresent(); }
    public boolean isDancing() { return this.entityData.get(DANCING); }
    public boolean isSkeleton() { return this.entityData.get(SKELETON); }
    public void setSkeleton(boolean v) { this.entityData.set(SKELETON, v); }
}