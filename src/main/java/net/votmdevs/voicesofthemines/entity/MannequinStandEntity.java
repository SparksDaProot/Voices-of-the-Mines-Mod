package net.votmdevs.voicesofthemines.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MannequinStandEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MannequinStandEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
    @Override public boolean isPushable() { return false; }
}