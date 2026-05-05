package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import org.jetbrains.annotations.Nullable;

public class SafeBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public SafeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SafeBlockEntity safe)) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() == VoicesOfTheMines.STETOSCOPE.get() && player.isShiftKeyDown()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (safe.doorState > 0 || safe.passcode.isEmpty()) {
            if (!stack.isEmpty()) {
                if (safe.storedItem.isEmpty()) {
                    if (!level.isClientSide) {
                        safe.storedItem = stack.copy();
                        stack.setCount(0);
                        safe.setChanged();
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                        player.displayClientMessage(Component.literal("§aItem stored in Safe."), true);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    if (!level.isClientSide)
                        player.displayClientMessage(Component.literal("§cSafe is already full!"), true);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (safe.doorState == 0) {
            if (safe.passcode.isEmpty() && player.isShiftKeyDown()) {
                if (level.isClientSide) {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.SafeScreen(pos, true));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (!safe.passcode.isEmpty()) {
                if (level.isClientSide) {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.SafeScreen(pos, false));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SafeBlockEntity(pos, state); }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, VoicesOfTheMines.SAFE_BE.get(), SafeBlockEntity::tick);
    }
}