package net.votmdevs.voicesofthemines.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class DriveBoxItem extends BlockItem {
    public DriveBoxItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        boolean hasDrives = false;

        if (stack.hasTag() && stack.getTag().contains("Inventory")) {
            CompoundTag inventoryTag = stack.getTag().getCompound("Inventory");
            ItemStackHandler handler = new ItemStackHandler(6);
            handler.deserializeNBT(inventoryTag);

            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack diskStack = handler.getStackInSlot(i);
                if (!diskStack.isEmpty() && diskStack.hasTag()) {
                    if (!hasDrives) {
                        tooltip.add(Component.literal("Drives:").withStyle(ChatFormatting.GRAY));
                        hasDrives = true;
                    }

                    CompoundTag diskTag = diskStack.getTag();
                    String sigType = diskTag.getString("SignalType");
                    int sigLevel = diskTag.getInt("SignalLevel");

                    // exp planet_mercury -> Planet Mercury
                    String formattedType = sigType.replace("planet_", "Planet ").replace("_", " ");
                    String[] words = formattedType.split(" ");
                    StringBuilder nameBuilder = new StringBuilder();
                    for (String word : words) {
                        if (word.length() > 0) {
                            nameBuilder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
                        }
                    }
                    String finalName = nameBuilder.toString().trim();

                    // colors
                    ChatFormatting color;
                    if (sigLevel == 0) color = ChatFormatting.GRAY;
                    else if (sigLevel == 1) color = ChatFormatting.WHITE;
                    else if (sigLevel == 2) color = ChatFormatting.AQUA;
                    else color = ChatFormatting.GOLD;

                    tooltip.add(Component.literal("- " + finalName).withStyle(color));
                }
            }
        }

        if (!hasDrives) {
            tooltip.add(Component.literal("Drives: Empty").withStyle(ChatFormatting.GRAY));
        }
    }
}