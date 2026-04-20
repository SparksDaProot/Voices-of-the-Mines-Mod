package net.votmdevs.voicesofthemines.entity;

import net.votmdevs.voicesofthemines.KerfurSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class CockroachEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Boolean> IS_SPAWNING = SynchedEntityData.defineId(CockroachEntity.class, EntityDataSerializers.BOOLEAN);

    public CockroachEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_SPAWNING, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, MaxwellEntity.class, true));

        this.goalSelector.addGoal(2, new EatFoodGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D, 1.0F));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.isSpawning() && this.tickCount > 20) {
                this.setSpawning(false);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && !this.level().isClientSide()) {
            this.playSound(KerfurSounds.COCKROACH_EAT.get(), 1.0F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.2F);
            player.getFoodData().eat(1, 0.1F);
            this.discard();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            if (this.isSpawning()) {
                event.getController().setAnimation(RawAnimation.begin().thenPlay("spawn"));
            } else {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("idlemove"));
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    public boolean isSpawning() { return this.entityData.get(IS_SPAWNING); }
    public void setSpawning(boolean value) { this.entityData.set(IS_SPAWNING, value); }

    class EatFoodGoal extends Goal {
        private final CockroachEntity roach;
        private ItemEntity targetFood;

        public EatFoodGoal(CockroachEntity roach) {
            this.roach = roach;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.roach.tickCount % 10 != 0) return false;

            List<ItemEntity> items = this.roach.level().getEntitiesOfClass(ItemEntity.class, this.roach.getBoundingBox().inflate(10.0D), item -> item.getItem().getItem().isEdible());

            if (!items.isEmpty()) {
                this.targetFood = items.get(0);
                return true;
            }
            return false;
        }

        @Override
        public void tick() {
            if (this.targetFood != null && this.targetFood.isAlive()) {
                this.roach.getNavigation().moveTo(this.targetFood, 1.2D);

                if (this.roach.distanceToSqr(this.targetFood) < 2.0D) {
                    this.roach.playSound(KerfurSounds.COCKROACH_EAT.get(), 0.5F, 1.5F);

                    this.targetFood.getItem().shrink(1);
                    if (this.targetFood.getItem().isEmpty()) {
                        this.targetFood.discard();
                    }
                    this.targetFood = null;
                }
            }
        }
    }
}