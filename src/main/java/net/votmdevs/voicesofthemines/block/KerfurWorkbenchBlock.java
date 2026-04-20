package net.votmdevs.voicesofthemines.block;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.entity.FleshEntity;
import net.votmdevs.voicesofthemines.entity.GarbageEntity;
import net.votmdevs.voicesofthemines.entity.KerfurEntity;
import net.votmdevs.voicesofthemines.entity.OmegaKerfurEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class KerfurWorkbenchBlock extends Block {
    public static final IntegerProperty PARTS = IntegerProperty.create("parts", 0, 15);
    public static final EnumProperty<KerfurColor> COLOR = EnumProperty.create("color", KerfurColor.class);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public KerfurWorkbenchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PARTS, 0).setValue(COLOR, KerfurColor.NONE).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PARTS, COLOR, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    private void advanceCraft(Level level, BlockPos pos, BlockState state, int nextPart, ItemStack stack, Player player, SoundEvent sound) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(PARTS, nextPart), 3);
            if (!player.isCreative()) stack.shrink(1);
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private void dropItemOnTable(Level level, BlockPos pos, Item item) {
        if (!level.isClientSide) {
            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, new ItemStack(item));
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        int parts = state.getValue(PARTS);
        KerfurColor color = state.getValue(COLOR);
        Item item = stack.getItem();

// KERFUR-O
        if (parts == 3 && item == VoicesOfTheMines.KERFUR_PART.get() && color == KerfurColor.NONE) {
            advanceCraft(level, pos, state, 7, stack, player, SoundEvents.WOOD_PLACE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (parts == 7 && item == VoicesOfTheMines.RECYCLED_PLASTIC.get()) {
            advanceCraft(level, pos, state, 8, stack, player, SoundEvents.WOOD_PLACE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (parts == 8 && item == VoicesOfTheMines.RECYCLED_PLASTIC.get()) {
            advanceCraft(level, pos, state, 9, stack, player, SoundEvents.WOOD_PLACE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (parts == 9 && item == VoicesOfTheMines.RECYCLED_RUBBER.get()) {
            advanceCraft(level, pos, state, 10, stack, player, SoundEvents.SLIME_BLOCK_PLACE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (parts == 10 && item == VoicesOfTheMines.METAL_SCRAP.get()) {
            advanceCraft(level, pos, state, 11, stack, player, SoundEvents.ANVIL_PLACE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (parts == 11 && item == VoicesOfTheMines.ELECTRONIC_WASTE.get()) {
            advanceCraft(level, pos, state, 12, stack, player, SoundEvents.COPPER_PLACE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (parts == 12 && item == VoicesOfTheMines.ELECTRONIC_WASTE.get()) {
            advanceCraft(level, pos, state, 13, stack, player, SoundEvents.COPPER_PLACE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (parts == 13 && item == VoicesOfTheMines.RADIOACTIVE_CAPSULE.get()) {
            advanceCraft(level, pos, state, 14, stack, player, SoundEvents.BEACON_ACTIVATE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (parts == 14 && color == KerfurColor.NONE) {
            KerfurColor newColor = getColorFromPainter(item);
            if (newColor != KerfurColor.NONE) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.setValue(PARTS, 15).setValue(COLOR, newColor), 3);
                    if (!player.isCreative()) stack.shrink(1);
                    level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        if (parts == 15 && stack.isEmpty()) {
            if (!level.isClientSide) {
                OmegaKerfurEntity omega = VoicesOfTheMines.OMEGA_KERFUR.get().create(level);
                if (omega != null) {
                    omega.setKerfurColor(color.getSerializedName());
                    omega.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player.getYRot(), 0.0F);
                    omega.setHealth(omega.getMaxHealth() - 3.0F);
                    omega.setDeactivated(true);
                    level.addFreshEntity(omega);
                }
                level.setBlock(pos, state.setValue(PARTS, 0).setValue(COLOR, KerfurColor.NONE), 3);
                level.playSound(null, pos, VotmSounds.CRAFT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

// common kerfur
        if (item == VoicesOfTheMines.KERFUR_PART.get() && parts >= 0 && parts < 3) {
            advanceCraft(level, pos, state, parts + 1, stack, player, SoundEvents.WOOD_PLACE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (parts == 3 && stack.isEmpty()) {
            if (!level.isClientSide) {
                boolean isAbandoned = false;
                if (color == KerfurColor.NONE) {
                    java.util.List<FleshEntity> fleshes = level.getEntitiesOfClass(FleshEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(1.5D));
                    if (!fleshes.isEmpty()) {
                        isAbandoned = true;
                        fleshes.get(0).discard();
                    }
                }

                if (color != KerfurColor.NONE || isAbandoned) {
                    KerfurEntity kerfur = VoicesOfTheMines.KERFUR.get().create(level);
                    if (kerfur != null) {
                        kerfur.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player.getYRot(), 0.0F);
                        kerfur.setKerfurColor(isAbandoned ? "abandoned" : color.getSerializedName());
                        level.addFreshEntity(kerfur);
                    }
                    level.setBlock(pos, state.setValue(PARTS, 0).setValue(COLOR, KerfurColor.NONE), 3);
                    level.playSound(null, pos, VotmSounds.CRAFT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (parts == 3 && color == KerfurColor.NONE) {
            KerfurColor newColor = getColorFromPainter(item);
            if (newColor != KerfurColor.NONE) {
                advanceCraft(level, pos, state, parts, stack, player, SoundEvents.DYE_USE);
                level.setBlock(pos, state.setValue(COLOR, newColor), 3);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

// ingredients
        if (!stack.isEmpty() && color == KerfurColor.NONE && parts != 3 && parts < 7) {

            // plastic
            if (item instanceof RecordItem) {
                if (parts == 0) { advanceCraft(level, pos, state, 4, stack, player, SoundEvents.ITEM_FRAME_ADD_ITEM); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 4) { advanceCraft(level, pos, state, 5, stack, player, SoundEvents.ITEM_FRAME_ADD_ITEM); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 5) { advanceCraft(level, pos, state, 6, stack, player, SoundEvents.ITEM_FRAME_ADD_ITEM); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 6) {
                    advanceCraft(level, pos, state, 0, stack, player, VotmSounds.CRAFT.get());
                    dropItemOnTable(level, pos, VoicesOfTheMines.RECYCLED_PLASTIC.get());
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // rubber
            if (item == Items.LEAD) {
                if (parts == 0) { advanceCraft(level, pos, state, 4, stack, player, SoundEvents.LEASH_KNOT_PLACE); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 4) { advanceCraft(level, pos, state, 5, stack, player, SoundEvents.LEASH_KNOT_PLACE); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 5) { advanceCraft(level, pos, state, 6, stack, player, SoundEvents.LEASH_KNOT_PLACE); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 6) {
                    advanceCraft(level, pos, state, 0, stack, player, VotmSounds.CRAFT.get());
                    dropItemOnTable(level, pos, VoicesOfTheMines.RECYCLED_RUBBER.get());
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // metal
            if (item == Items.IRON_INGOT || item == Items.COPPER_INGOT || item == Items.RAW_IRON || item == Items.RAW_COPPER) {
                if (parts == 0) { advanceCraft(level, pos, state, 4, stack, player, SoundEvents.METAL_PLACE); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 4) { advanceCraft(level, pos, state, 5, stack, player, SoundEvents.METAL_PLACE); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 5) { advanceCraft(level, pos, state, 6, stack, player, SoundEvents.METAL_PLACE); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 6) {
                    advanceCraft(level, pos, state, 0, stack, player, VotmSounds.CRAFT.get());
                    dropItemOnTable(level, pos, VoicesOfTheMines.METAL_SCRAP.get());
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // electro
            if (item == Items.JUKEBOX || item == Items.REDSTONE_LAMP || item == Items.OBSERVER || item == Items.PISTON) {
                if (parts == 0) { advanceCraft(level, pos, state, 4, stack, player, SoundEvents.STONE_PLACE); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 4) { advanceCraft(level, pos, state, 5, stack, player, SoundEvents.STONE_PLACE); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 5) { advanceCraft(level, pos, state, 6, stack, player, SoundEvents.STONE_PLACE); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 6) {
                    advanceCraft(level, pos, state, 0, stack, player, VotmSounds.CRAFT.get());
                    dropItemOnTable(level, pos, VoicesOfTheMines.ELECTRONIC_WASTE.get());
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // garbage
            if (getColorFromPainter(item) == KerfurColor.NONE && item != VoicesOfTheMines.KERFUR_PART.get()) {
                if (parts == 0) { advanceCraft(level, pos, state, 4, stack, player, VotmSounds.GARBAGE_DROP.get()); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 4) { advanceCraft(level, pos, state, 5, stack, player, VotmSounds.GARBAGE_DROP.get()); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 5) { advanceCraft(level, pos, state, 6, stack, player, VotmSounds.GARBAGE_DROP.get()); return InteractionResult.sidedSuccess(level.isClientSide); }
                if (parts == 6) {
                    if (!level.isClientSide) {
                        if (!player.isCreative()) stack.shrink(1);
                        GarbageEntity garbage = VoicesOfTheMines.GARBAGE.get().create(level);
                        if (garbage != null) {
                            garbage.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player.getYRot(), 0.0F);
                            level.addFreshEntity(garbage);
                        }
                        level.setBlock(pos, state.setValue(PARTS, 0), 3);
                        level.playSound(null, pos, VotmSounds.CRAFT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        return InteractionResult.PASS;
    }

    private KerfurColor getColorFromPainter(Item item) {
        if (item == VoicesOfTheMines.PAINTER_BLACK.get()) return KerfurColor.BLACK;
        if (item == VoicesOfTheMines.PAINTER_BLUE.get()) return KerfurColor.BLUE;
        if (item == VoicesOfTheMines.PAINTER_GREEN.get()) return KerfurColor.GREEN;
        if (item == VoicesOfTheMines.PAINTER_PINK.get()) return KerfurColor.PINK;
        if (item == VoicesOfTheMines.PAINTER_RED.get()) return KerfurColor.RED;
        if (item == VoicesOfTheMines.PAINTER_WHITE.get()) return KerfurColor.WHITE;
        if (item == VoicesOfTheMines.PAINTER_YELLOW.get()) return KerfurColor.YELLOW;
        return KerfurColor.NONE;
    }
}