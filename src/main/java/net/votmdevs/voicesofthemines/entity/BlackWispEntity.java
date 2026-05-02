package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class BlackWispEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BlackWispEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    protected SoundEvent getAmbientSound() {
        // Рандомно выбираем один из 3 звуков
        int rand = this.random.nextInt(3);
        return switch (rand) {
            case 0 -> VotmSounds.BLACKWISP1.get();
            case 1 -> VotmSounds.BLACKWISP2.get();
            default -> VotmSounds.BLACKWISP3.get();
        };
    }

    @Override
    public float getVoicePitch() {
        return 0.8F + this.random.nextFloat() * 0.4F;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            // КЛИЕНТ: Включаем эффекты, если игрок рядом
            Player localPlayer = net.minecraft.client.Minecraft.getInstance().player;
            if (localPlayer != null && !localPlayer.isCreative() && !localPlayer.isSpectator() && this.isAlive()) {
                double dist = this.distanceTo(localPlayer);
                if (dist < 20.0D) {
                    VoicesOfTheMines.ClientForgeEvents.wispShakeTimer = 2; // Трясем экран
                }
                if (dist < 1.5D) {
                    if (VoicesOfTheMines.ClientForgeEvents.wispBlackScreenTimer <= 0) {
                        VoicesOfTheMines.ClientForgeEvents.wispBlackScreenTimer = 80; // 4 секунды черного экрана
                    }
                }
            }
        } else {
            AABB pullBox = this.getBoundingBox().inflate(20.0D);
            List<Entity> entities = this.level().getEntities(this, pullBox, e -> e instanceof LivingEntity || e instanceof ItemEntity);

            Player targetPlayer = null;
            double closestPlayerDist = 21.0D;

            for (Entity e : entities) {
                if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;

                double dx = this.getX() - e.getX();
                double dy = (this.getY() + this.getBbHeight() / 2.0) - (e.getY() + e.getBbHeight() / 2.0);
                double dz = this.getZ() - e.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (dist < 20.0D) {
                    if (e instanceof Player p) {
                        if (dist < closestPlayerDist) {
                            closestPlayerDist = dist;
                            targetPlayer = p;
                        }
                    }

                    double pullStrength = 0.05D + (0.12D * (1.0D - dist / 20.0D));
                    e.setDeltaMovement(e.getDeltaMovement().add(dx * pullStrength / dist, dy * pullStrength / dist, dz * pullStrength / dist));
                    e.hurtMarked = true;

                    if (dist < 1.5D) {
                        if (e instanceof Player player) {
                            if (!player.getPersistentData().contains("WispDoomTimer")) {
                                player.getPersistentData().putInt("WispDoomTimer", 80);

                                this.playSound(VotmSounds.WISPBLACKSCREEN.get(), 2.0F, 1.0F);
                            }
                        } else {
                            e.kill();
                        }
                    }
                }
            }

            if (targetPlayer != null) {
                this.getNavigation().moveTo(targetPlayer, 1.2D);
            }

            for (int i = 0; i < 3; i++) {
                if (this.random.nextInt(4) == 0) {
                    int rx = (int) (this.getX() + this.random.nextInt(41) - 20);
                    int ry = (int) (this.getY() + this.random.nextInt(41) - 20);
                    int rz = (int) (this.getZ() + this.random.nextInt(41) - 20);
                    BlockPos targetPos = new BlockPos(rx, ry, rz);
                    BlockState state = this.level().getBlockState(targetPos);

                    if (!state.isAir() && state.getDestroySpeed(this.level(), targetPos) >= 0.0F) {
                        this.level().destroyBlock(targetPos, true);
                    }
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event ->
                event.setAndContinue(RawAnimation.begin().thenLoop("idle"))
        ));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}