package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import org.jetbrains.annotations.Nullable;

public class ConsoleBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ConsoleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            boolean isBaseBroken = false;
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-50, -20, -50), pos.offset(50, 20, 50))) {
                BlockState st = level.getBlockState(p);
                if (st.getBlock() == VoicesOfTheMines.SERVER_BLOCK.get() &&
                        st.getValue(ServerBlock.TYPE) == ServerType.BASE &&
                        st.getValue(ServerBlock.BROKEN)) {
                    isBaseBroken = true;
                    break;
                }
            }
            if (isBaseBroken) {
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.NotificationPacket("ERR: BASE SERVER OFFLINE. CONNECTION LOST."),
                        serverPlayer.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
                level.playSound(null, pos, VotmSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.5F);
                return InteractionResult.SUCCESS;
            }
        }

        if (level.isClientSide()) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.ConsoleScreen(pos));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
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

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConsoleBlockEntity(pos, state);
    }
}