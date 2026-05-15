package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import org.jetbrains.annotations.Nullable;

public class TapeRecorderBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    // hitbox
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 9.0D, 15.0D);

    public TapeRecorderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, VoicesOfTheMines.TAPE_RECORDER_BE.get(),
                (lvl, pos, st, blockEntity) -> blockEntity.tick());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.isEmpty()) {
                if (player.isShiftKeyDown() && state.getValue(OPEN)) {
                    // shift+ empty hand
                    if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TapeRecorderBlockEntity be) {
                        if (be.isRecording) {
                            be.stopAmbientRecordingAndBroadcast();
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Broadcast sent!").withStyle(net.minecraft.ChatFormatting.GREEN), true);
                        } else {
                            be.startAmbientRecording();
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Recording started...").withStyle(net.minecraft.ChatFormatting.RED), true);
                        }
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                } else {
                    if (!level.isClientSide()) {
                        boolean isOpen = state.getValue(OPEN);
                        level.setBlock(pos, state.setValue(OPEN, !isOpen), 3);
                        net.minecraft.sounds.SoundEvent sound = isOpen ? net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE : net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN;
                        level.playSound(null, pos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
            else if (heldItem.getItem() == VoicesOfTheMines.CASSETTE.get() && state.getValue(OPEN)) {
                // cassette & gui
                if (!level.isClientSide() && player instanceof ServerPlayer sp && level.getBlockEntity(pos) instanceof TapeRecorderBlockEntity be) {
                    StringBuilder dataBuilder = new StringBuilder();
                    for (TapeRecorderBlockEntity loadedBe : TapeRecorderBlockEntity.LOADED_RECORDERS) {
                        BlockPos bePos = loadedBe.getBlockPos();
                        double dist = Math.sqrt(bePos.distSqr(pos));
                        boolean isClosed = !loadedBe.getBlockState().getValue(OPEN);

                        dataBuilder.append(bePos.getX()).append(",").append(bePos.getY()).append(",").append(bePos.getZ())
                                .append("|").append(loadedBe.customName)
                                .append("|").append(Math.round(dist))
                                .append("|").append(isClosed).append(";");
                    }

                    if (!player.isCreative()) heldItem.shrink(1);

                    be.isRecording = true;
                    be.syncData();

                    KerfurPacketHandler.INSTANCE.sendTo(new KerfurPacketHandler.OpenTapeGuiPacket(dataBuilder.toString(), pos), sp.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TapeRecorderBlockEntity(pos, state);
    }
}