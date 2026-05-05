package net.votmdevs.voicesofthemines.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;

public class SleepinessEffect extends MobEffect {

    public SleepinessEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof ServerPlayer player) {
            net.minecraft.world.effect.MobEffectInstance instance = player.getEffect(VoicesOfTheMines.SLEEPINESS.get());

            if (instance != null && instance.getDuration() < 1100) {
                if (!player.getPersistentData().getBoolean("IsFallingAsleep")) {
                    if (player.getRandom().nextInt(150) == 0) {
                        player.getPersistentData().putBoolean("IsFallingAsleep", true);

                        KerfurPacketHandler.INSTANCE.sendTo(new KerfurPacketHandler.TriggerSleepAnimPacket(),
                                player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}