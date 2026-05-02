package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import org.jetbrains.annotations.Nullable;

public class PlushieBlock extends BaseEntityBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final PlushieType type;

    protected static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(3.5D, 0.0D, 4.0D, 12.5D, 14.0D, 12.0D);
    protected static final VoxelShape SHAPE_EAST_WEST = Block.box(4.0D, 0.0D, 3.5D, 12.0D, 14.0D, 12.5D);

    public PlushieBlock(Properties properties, PlushieType type) {
        super(properties);
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public PlushieType getType() { return this.type; }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return (dir == Direction.EAST || dir == Direction.WEST) ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // packet
            KerfurPacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.NEAR.with(
                            () -> new net.minecraftforge.network.PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 30.0D, level.dimension())
                    ),
                    new KerfurPacketHandler.PlushieInteractPacket(pos)
            );

            level.playSound(null, pos, net.votmdevs.voicesofthemines.VotmSounds.PLUSHBEEP.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);

        } else {
            // client
            if (this.type.getSecretText() != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PlushieBlockEntity plushie) {
                    plushie.activateSecretText();
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlushieBlockEntity(pos, state, this.type); // Передаем тип в BlockEntity!
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return createTickerHelper(type, net.votmdevs.voicesofthemines.VoicesOfTheMines.PLUSHIE_BE.get(), net.votmdevs.voicesofthemines.block.PlushieBlockEntity::tick);
    }
}