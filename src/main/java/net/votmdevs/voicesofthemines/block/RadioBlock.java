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

public class RadioBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected static final VoxelShape SHAPE_NS = Block.box(3.0D, 0.0D, 4.5D, 13.0D, 7.0D, 11.5D);
    protected static final VoxelShape SHAPE_EW = Block.box(4.5D, 0.0D, 3.0D, 11.5D, 7.0D, 13.0D);

    public RadioBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return (dir == Direction.EAST || dir == Direction.WEST) ? SHAPE_EW : SHAPE_NS;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RadioBlockEntity radio) {
                if (player.isShiftKeyDown()) {
                    String nextTrack = net.votmdevs.voicesofthemines.client.ClientRadioManager.getNextTrack(radio.currentTrack);
                    if (!nextTrack.isEmpty()) {
                        KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.RadioActionPacket(pos, 1, nextTrack));
                        // hotbar text
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Playing: " + nextTrack)
                                .withStyle(net.minecraft.ChatFormatting.getById(level.random.nextInt(15) + 1)), true);
                    } else {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Folder votm_radio is empty!").withStyle(net.minecraft.ChatFormatting.RED), true);
                    }
                } else {
                    KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.RadioActionPacket(pos, 0, ""));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadioBlockEntity(pos, state);
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
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
}