package net.votmdevs.voicesofthemines.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HostileMannequinEntity extends AbstractMannequinEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 = Start Pose, 1 = Moving, 2 = Random Pose, 3 = On Fire (Attacking)
    public static final EntityDataAccessor<Integer> ANIM_STATE = SynchedEntityData.defineId(HostileMannequinEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> POSE_INDEX = SynchedEntityData.defineId(HostileMannequinEntity.class, EntityDataSerializers.INT);

    private boolean hasLeftStand = false;
    private boolean isPermanentlyAggro = false;

    private int moveCooldown = 0;
    private int attackCooldown = 0;

    public HostileMannequinEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIM_STATE, 0);
        this.entityData.define(POSE_INDEX, 1);
    }

    private boolean isPlayerLookingAtMe(Player player) {
        Vec3 viewVector = player.getViewVector(1.0F).normalize();
        Vec3 vectorToEntity = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        if (vectorToEntity.length() > 64.0D) return false;
        vectorToEntity = vectorToEntity.normalize();
        double dotProduct = viewVector.dot(vectorToEntity);
        return dotProduct > 0.3D && player.hasLineOfSight(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() || this.isHeld()) return;

        Player player = this.level().getNearestPlayer(this, 32.0D);
        int state = this.entityData.get(ANIM_STATE);

        if (this.isOnFire() && !isPermanentlyAggro) {
            isPermanentlyAggro = true;
        }

        if (attackCooldown > 0) attackCooldown--;

        boolean isValidTarget = player != null && !player.isCreative() && !player.isSpectator();

        if (isPermanentlyAggro) {
            this.entityData.set(ANIM_STATE, 3);
            if (isValidTarget) {
                if (this.tickCount % 10 == 0) {
                    this.getNavigation().moveTo(player, 1.3D);
                }

                if (attackCooldown == 0 && this.distanceTo(player) < 2.0D) {
                    boolean hurt = player.hurt(this.damageSources().mobAttack(this), 4.0F); // 2 сердечка
                    if (!hurt) {
                        player.hurt(this.damageSources().generic(), 4.0F); // Пробиваем "чистым" уроном, если заблокировано
                    }

                    player.knockback(0.5D, this.getX() - player.getX(), this.getZ() - player.getZ());


                    this.playSound(net.votmdevs.voicesofthemines.VotmSounds.MANNEQUIN_ATTACK.get(), 1.0F, 1.0F);
                    attackCooldown = 20;
                }
            } else {
                this.getNavigation().stop();
            }
        } else if (isValidTarget) {
            boolean looking = isPlayerLookingAtMe(player);

            if (looking) {
                if (state == 1) {
                    this.getNavigation().stop();
                    this.entityData.set(ANIM_STATE, 2);
                    this.entityData.set(POSE_INDEX, this.random.nextInt(10) + 1);
                }
            } else {
                if (state == 0 || state == 2) {
                    if (moveCooldown <= 0) {
                        if (this.random.nextInt(100) < 5) {
                            this.entityData.set(ANIM_STATE, 1);

                            if (!hasLeftStand && state == 0) {
                                hasLeftStand = true;
                                MannequinStandEntity stand = VoicesOfTheMines.MANNEQUIN_STAND.get().create(this.level());
                                if (stand != null) {
                                    stand.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0);
                                    this.level().addFreshEntity(stand);
                                }
                            }
                        }
                    } else {
                        moveCooldown--;
                    }
                } else if (state == 1) {
                    if (this.tickCount % 10 == 0) {
                        this.getNavigation().moveTo(player, 1.2D);
                    }
                    if (this.random.nextInt(100) < 2 && this.distanceTo(player) > 3.0D) {
                        this.getNavigation().stop();
                        this.entityData.set(ANIM_STATE, 2);
                        this.entityData.set(POSE_INDEX, this.random.nextInt(10) + 1);
                        moveCooldown = 40 + this.random.nextInt(100);
                    }
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            int state = this.entityData.get(ANIM_STATE);
            if (state == 0) return event.setAndContinue(RawAnimation.begin().thenLoop("pose_start"));
            if (state == 1 || state == 3) return event.setAndContinue(RawAnimation.begin().thenLoop("run"));

            int pose = this.entityData.get(POSE_INDEX);
            return event.setAndContinue(RawAnimation.begin().thenLoop("pose" + pose));
        }));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HasLeftStand", this.hasLeftStand);
        tag.putBoolean("PermanentlyAggro", this.isPermanentlyAggro);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hasLeftStand = tag.getBoolean("HasLeftStand");
        this.isPermanentlyAggro = tag.getBoolean("PermanentlyAggro");
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
    @Override public boolean isPushable() { return false; }
}