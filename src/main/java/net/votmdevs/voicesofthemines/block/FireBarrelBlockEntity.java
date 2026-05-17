package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FireBarrelBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public int igniteTimer = -1;

    public FireBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.FIRE_BARREL_BE.get(), pos, state);
    }

    public void ignite() {
        if (igniteTimer == -1 && level != null) {
            igniteTimer = 40; // 2 sec
            level.playSound(null, getBlockPos(), SoundEvents.NETHERITE_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 2.0f);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FireBarrelBlockEntity entity) {
        if (entity.igniteTimer > 0) {
            entity.igniteTimer--;

            if (level instanceof ServerLevel sl) {
                // Партиклы искр/огня перед взрывом
                sl.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 3, 0.1, 0.1, 0.1, 0.05);
            }

            if (entity.igniteTimer == 0) {
                level.removeBlock(pos, false);
                level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6.0F, true, Level.ExplosionInteraction.NONE);
            }
        }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}