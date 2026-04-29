package net.votmdevs.voicesofthemines.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.votmdevs.voicesofthemines.client.gui.PaperSheetScreen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaperSheetItem extends Item {
    public PaperSheetItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (stack.hasTag() && stack.getTag().getBoolean("Written")) {
            tooltip.add(Component.literal("There's something written in it").withStyle(ChatFormatting.WHITE));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new PaperSheetScreen(hand, stack));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}