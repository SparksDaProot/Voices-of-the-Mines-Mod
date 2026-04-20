package net.votmdevs.voicesofthemines.block;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.entity.GarbageEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrashBinBlock extends Block {
    public static final IntegerProperty STORED = IntegerProperty.create("stored", 0, 10);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public TrashBinBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STORED, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STORED, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        int stored = state.getValue(STORED);

        if (!level.isClientSide) {
            boolean absorbed = false;

            if (hit.getDirection() == Direction.UP) {
                List<GarbageEntity> garbages = level.getEntitiesOfClass(GarbageEntity.class, player.getBoundingBox().inflate(5.0D));
                for (GarbageEntity garbage : garbages) {
                    if (player.getUUID().equals(garbage.getHeldBy().orElse(null))) {
                        int levelToAbsorb = garbage.getGarbageLevel();

                        if (stored + levelToAbsorb <= 10) {
                            level.setBlock(pos, state.setValue(STORED, stored + levelToAbsorb).setValue(FACING, state.getValue(FACING)), 3);
                            garbage.discard();
                            level.playSound(null, pos, VotmSounds.GARBAGE_DROP.get(), SoundSource.BLOCKS, 1.0F, 0.8F);
                            absorbed = true;
                            break;
                        }
                    }
                }
            }

            // Убрали звук раздатчика
            if (!absorbed && player.getItemInHand(hand).isEmpty() && stored > 0) {
                level.scheduleTick(pos, this, 5);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int stored = state.getValue(STORED);
        if (stored > 0) {
            GarbageEntity garbage = VoicesOfTheMines.GARBAGE.get().create(level);
            if (garbage != null) {
                garbage.setGarbageLevel(1);
                garbage.moveTo(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, random.nextFloat() * 360F, 0.0F);
                garbage.setDeltaMovement((random.nextDouble() - 0.5) * 0.3, 0.4, (random.nextDouble() - 0.5) * 0.3);
                level.addFreshEntity(garbage);

                // Оставили только звук вылета
                level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 1.0F, 0.5F + random.nextFloat() * 0.5F);
            }

            int nextStored = stored - 1;
            // Не забываем сохранять поворот при обновлении!
            level.setBlock(pos, state.setValue(STORED, nextStored).setValue(FACING, state.getValue(FACING)), 3);

            if (nextStored > 0) {
                level.scheduleTick(pos, this, 10);
            }
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            ItemStack stack = new ItemStack(VoicesOfTheMines.TRASH_BIN_ITEM.get());
            int stored = state.getValue(STORED);

            if (stored > 0) {
                stack.getOrCreateTag().putInt("StoredGarbage", stored);
            }

            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return java.util.Collections.emptyList();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("StoredGarbage")) {
            int stored = stack.getTag().getInt("StoredGarbage");
            level.setBlock(pos, state.setValue(STORED, stored).setValue(FACING, state.getValue(FACING)), 3);
        }
    }
}