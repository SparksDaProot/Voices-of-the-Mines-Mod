package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class KeypadBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;


    protected static final VoxelShape SHAPE_NORTH = Block.box(4.0D, 0.0D, 14.0D, 12.0D, 12.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 12.0D, 2.0D);
    protected static final VoxelShape SHAPE_WEST = Block.box(14.0D, 0.0D, 4.0D, 16.0D, 12.0D, 12.0D);
    protected static final VoxelShape SHAPE_EAST = Block.box(0.0D, 0.0D, 4.0D, 2.0D, 12.0D, 12.0D);

    public KeypadBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case EAST: return SHAPE_EAST;
            case SOUTH: return SHAPE_SOUTH;
            case WEST: return SHAPE_WEST;
            case NORTH: default: return SHAPE_NORTH;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KeypadBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof KeypadBlockEntity keypad) {
                KeypadBlockEntity.tick(lvl, pos, st, keypad);
            }
        };
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KeypadBlockEntity keypad) {

            Vec3 hitLoc = hit.getLocation();
            double localX = hitLoc.x - pos.getX();
            double localY = hitLoc.y - pos.getY();
            double localZ = hitLoc.z - pos.getZ();

            Direction facing = state.getValue(FACING);
            double faceX = 0;

            switch (facing) {
                case NORTH -> faceX = 1.0 - localX;
                case SOUTH -> faceX = localX;
                case WEST -> faceX = localZ;
                case EAST -> faceX = 1.0 - localZ;
            }

            int pressedButton = -1;


            if (localY > 0.45 && localY < 0.65) {
                // 1 2 3
                if (faceX < 0.42) pressedButton = 1;
                else if (faceX < 0.58) pressedButton = 2;
                else pressedButton = 3;
            } else if (localY > 0.25 && localY <= 0.45) {
                // 4 5 6
                if (faceX < 0.42) pressedButton = 4;
                else if (faceX < 0.58) pressedButton = 5;
                else pressedButton = 6;
            } else if (localY > 0.05 && localY <= 0.25) {
                // 7 8 9
                if (faceX < 0.42) pressedButton = 7;
                else if (faceX < 0.58) pressedButton = 8;
                else pressedButton = 9;
            }

            if (pressedButton != -1) {
                keypad.pressButton(pressedButton);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}