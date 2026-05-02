package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class GreenWispEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GreenWispEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new GreenWispTeleportGoal(this)); // ИИ телепортации
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return VotmSounds.PINK_IDLE.get();
    }

    @Override
    public void playAmbientSound() {
        super.playAmbientSound();
    }

    @Override
    public float getVoicePitch() {
        return 0.8F + this.random.nextFloat() * 0.4F;
    }

    static class GreenWispTeleportGoal extends Goal {
        private final GreenWispEntity wisp;
        private Player targetPlayer = null;

        public GreenWispTeleportGoal(GreenWispEntity wisp) {
            this.wisp = wisp;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player closest = wisp.level().getNearestPlayer(wisp, 20.0D);
            if (closest != null && closest.isAlive() && !closest.isCreative() && !closest.isSpectator()) {
                this.targetPlayer = closest;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return targetPlayer != null && targetPlayer.isAlive() && !targetPlayer.isCreative() && !targetPlayer.isSpectator() && wisp.distanceTo(targetPlayer) <= 25.0D;
        }

        @Override
        public void start() {
            wisp.getNavigation().moveTo(targetPlayer, 1.2D);
        }

        @Override
        public void tick() {
            if (targetPlayer == null) return;

            wisp.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);
            wisp.getNavigation().moveTo(targetPlayer, 1.2D);

            if (wisp.distanceTo(targetPlayer) < 1.5D) {
                teleportTarget(targetPlayer);
                this.stop();
            }
        }

        private void teleportTarget(Player player) {
            Level level = wisp.level();
            if (level instanceof ServerLevel serverLevel) {
                double oldX = player.getX();
                double oldY = player.getY();
                double oldZ = player.getZ();

                for (int i = 0; i < 16; i++) {
                    double tx = player.getX() + (wisp.random.nextDouble() - 0.5D) * 200.0D;
                    double ty = net.minecraft.util.Mth.clamp(player.getY() + (wisp.random.nextInt(64) - 32), level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
                    double tz = player.getZ() + (wisp.random.nextDouble() - 0.5D) * 200.0D;

                    // randomTeleport
                    if (player.randomTeleport(tx, ty, tz, true)) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, oldX, oldY + 1.0D, oldZ, 30, 0.5, 1.0, 0.5, 0.1);
                        level.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);

                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5, 1.0, 0.5, 0.1);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);

                        break;
                    }
                }
            }
        }

        @Override
        public void stop() {
            targetPlayer = null;
            wisp.getNavigation().stop();
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