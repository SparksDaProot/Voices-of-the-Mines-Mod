package net.votmdevs.voicesofthemines.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.entity.CockroachEntity;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;

public class MaracasItem extends Item {
    public MaracasItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        if (mainHandItem.getItem() == VoicesOfTheMines.MARACAS.get() && offHandItem.getItem() == VoicesOfTheMines.MARACAS.get()) {

            player.swing(hand);

            if (!level.isClientSide) {
                net.minecraft.sounds.SoundEvent[] maracasSounds = {
                        VotmSounds.MARACAS_1.get(), VotmSounds.MARACAS_2.get(),
                        VotmSounds.MARACAS_3.get(), VotmSounds.MARACAS_4.get()
                };
                net.minecraft.sounds.SoundEvent selectedSound = maracasSounds[level.random.nextInt(maracasSounds.length)];

                level.playSound(null, player.blockPosition(), selectedSound, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.9F + (level.random.nextFloat() * 0.2F));
            }

            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}