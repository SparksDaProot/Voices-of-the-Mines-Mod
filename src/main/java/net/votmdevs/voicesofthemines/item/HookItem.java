package net.votmdevs.voicesofthemines.item;

import net.votmdevs.voicesofthemines.entity.HookEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HookItem extends Item {

    public HookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        boolean hasHook = player.level().getEntitiesOfClass(HookEntity.class, player.getBoundingBox().inflate(30.0D))
                .stream().anyMatch(h -> player.getUUID().equals(h.getOwnerUUID()));

        if (!hasHook) {
            if (!level.isClientSide) {
                HookEntity hook = new HookEntity(level, player);
                hook.setDeltaMovement(player.getLookAngle().scale(1.5D));
                level.addFreshEntity(hook);
                player.playSound(net.minecraft.sounds.SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F);

                stack.getOrCreateTag().putBoolean("Active", true);
            }

            player.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            boolean hasHook = player.level().getEntitiesOfClass(HookEntity.class, player.getBoundingBox().inflate(30.0D))
                    .stream().anyMatch(h -> player.getUUID().equals(h.getOwnerUUID()));

            stack.getOrCreateTag().putBoolean("Active", hasHook);
        }
    }
}