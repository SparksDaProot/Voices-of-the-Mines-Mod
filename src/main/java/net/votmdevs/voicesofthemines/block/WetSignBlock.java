package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import org.jetbrains.annotations.Nullable;

public class WetSignBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    // hitbox
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 1.0D, 13.0D, 16.0D, 15.0D);

    public WetSignBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !oldState.is(state.getBlock())) {
            int maxRadius = 3; // puddle radius

            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-maxRadius, -1, -maxRadius), pos.offset(maxRadius, 1, maxRadius))) {
                if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)) {
                    double dist = Math.sqrt(pos.distSqr(p));
                    if (dist <= maxRadius && dist > 0) {
                        double pct = dist / maxRadius;
                        int stateDist = 1;
                        if (pct > 0.75) stateDist = 4;
                        else if (pct > 0.50) stateDist = 3;
                        else if (pct > 0.25) stateDist = 2;

                        level.setBlock(p, net.votmdevs.voicesofthemines.VoicesOfTheMines.WET_PUDDLE_BLOCK.get().defaultBlockState().setValue(WetPuddleBlock.DISTANCE, stateDist), 3);
                    }
                }
            }
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            int maxRadius = 3; // also mush be changged radius here
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-maxRadius, -1, -maxRadius), pos.offset(maxRadius, 1, maxRadius))) {
                if (level.getBlockState(p).getBlock() == net.votmdevs.voicesofthemines.VoicesOfTheMines.WET_PUDDLE_BLOCK.get()) {
                    level.removeBlock(p, false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WetSignBlockEntity(pos, state); }
}