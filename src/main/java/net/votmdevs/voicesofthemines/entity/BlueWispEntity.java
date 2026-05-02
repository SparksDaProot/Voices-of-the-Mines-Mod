package net.votmdevs.voicesofthemines.entity;

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
import net.minecraft.world.phys.AABB;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class BlueWispEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BlueWispEntity(EntityType<? extends PathfinderMob> type, Level level) {
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
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return VotmSounds.BLACKWISP2.get();
    }

    @Override
    public void playAmbientSound() {
        super.playAmbientSound();
    }

    @Override
    public float getVoicePitch() {
        return 0.7F + this.random.nextFloat() * 0.3F;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            Player localPlayer = net.minecraft.client.Minecraft.getInstance().player;
            if (localPlayer != null && !localPlayer.isCreative() && !localPlayer.isSpectator() && this.isAlive()) {
                if (this.distanceTo(localPlayer) <= 10.0D) {
                    double friction = 0.8D;
                    double bobbing = Math.sin((this.tickCount + localPlayer.getId()) * 0.1D) * 0.04D;
                    double newYVelocity = 0.085D + bobbing;

                    localPlayer.setDeltaMovement(localPlayer.getDeltaMovement().x * friction, newYVelocity, localPlayer.getDeltaMovement().z * friction);
                    localPlayer.fallDistance = 0.0F;
                }
            }
        }
        else {
            AABB liftBox = this.getBoundingBox().inflate(10.0D);
            List<Entity> entities = this.level().getEntities(this, liftBox, e -> e.isAlive() && e != this);

            for (Entity e : entities) {
                if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;

                if (this.distanceTo(e) <= 10.0D) {
                    if (!(e instanceof Player)) {
                        double friction = 0.8D;
                        double bobbing = Math.sin((this.tickCount + e.getId()) * 0.1D) * 0.04D;
                        double newYVelocity = 0.05D + bobbing;

                        e.setDeltaMovement(e.getDeltaMovement().x * friction, newYVelocity, e.getDeltaMovement().z * friction);
                        e.hurtMarked = true;
                    }

                    e.fallDistance = 0.0F;
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